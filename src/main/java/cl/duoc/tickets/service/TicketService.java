package cl.duoc.tickets.service;

import cl.duoc.tickets.dto.EstadisticasEvento;
import cl.duoc.tickets.dto.TicketRequest;
import cl.duoc.tickets.dto.TicketUpdateRequest;
import cl.duoc.tickets.exception.TicketNoEncontradoException;
import cl.duoc.tickets.model.EstadoTicket;
import cl.duoc.tickets.model.Ticket;
import cl.duoc.tickets.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class TicketService {

    private final TicketRepository repo;
    private final PdfService pdf;
    private final EfsStorageService efs;
    private final S3StorageService s3;

    @Value("${app.efs.eliminar-tras-subir:false}")
    private boolean eliminarEfsTrasSubir;

    public TicketService(TicketRepository repo, PdfService pdf, EfsStorageService efs, S3StorageService s3) {
        this.repo = repo;
        this.pdf = pdf;
        this.efs = efs;
        this.s3 = s3;
    }

    // ---------- Generar ----------
    @Transactional
    public Ticket generar(TicketRequest r, boolean subirS3) {
        Ticket t = new Ticket();
        t.setEventoId(r.eventoId());
        t.setUsuarioId(r.usuarioId());
        t.setNombreAsistente(r.nombreAsistente());
        t.setTipoEntrada(r.tipoEntrada());
        t.setPrecio(r.precio());
        t.setEstado(r.estado() != null ? r.estado() : EstadoTicket.RESERVADO);
        t.setCodigo(UUID.randomUUID().toString().substring(0, 8).toUpperCase());

        escribirPdfEnEfs(t);                 // se genera y guarda en efs
        if (subirS3) {
            subirAS3Interno(t);              // subida automatica a s3
        }
        return repo.save(t);
    }

    // subir a s3
    @Transactional
    public Ticket subirAS3(Long id) {
        Ticket t = buscar(id);
        subirAS3Interno(t);
        return repo.save(t);
    }

    // buscar
    public Ticket buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new TicketNoEncontradoException("Ticket " + id + " no existe"));
    }

    public List<Ticket> listarPorEvento(String eventoId) {
        return repo.findByEventoId(eventoId);
    }

    // descargar
    public byte[] descargar(Ticket t) {
        if (t.getS3Key() != null) return s3.descargar(t.getS3Key());
        if (efs.existe(t.getEfsPath())) return efs.leer(t.getEfsPath());
        return pdf.generar(t);
    }

    public byte[] descargarZipPorEvento(String eventoId) {
        return zip(noVacia(repo.findByEventoId(eventoId), "No hay tickets para el evento " + eventoId));
    }

    public byte[] descargarZipPorUsuario(String eventoId, String usuarioId) {
        return zip(noVacia(repo.findByEventoIdAndUsuarioId(eventoId, usuarioId),
                "No hay tickets para el usuario " + usuarioId + " en el evento " + eventoId));
    }

    public String nombreArchivo(Ticket t) {
        return "ticket-" + t.getCodigo() + ".pdf";
    }

    // modificar
    @Transactional
    public Ticket actualizar(Long id, TicketUpdateRequest r) {
        Ticket t = buscar(id);
        if (r.nombreAsistente() != null) t.setNombreAsistente(r.nombreAsistente());
        if (r.tipoEntrada() != null) t.setTipoEntrada(r.tipoEntrada());
        if (r.precio() != null) t.setPrecio(r.precio());
        if (r.estado() != null) t.setEstado(r.estado());

        boolean estabaEnS3 = t.getS3Key() != null;
        escribirPdfEnEfs(t);                 // se genera el pdf con los datos nuevos
        if (estabaEnS3) subirAS3Interno(t);  // se reemplaza en S3
        return repo.save(t);
    }

    // eliminar
    @Transactional
    public void eliminar(Long id) {
        Ticket t = buscar(id);
        if (t.getS3Key() != null) s3.eliminar(t.getS3Key());
        efs.eliminarSiExiste(t.getEfsPath());
        repo.delete(t);
    }

    // estadisticas
    public EstadisticasEvento estadisticas(String eventoId) {
        List<Ticket> tickets = repo.findByEventoId(eventoId);
        long reservados = contar(tickets, EstadoTicket.RESERVADO);
        long vendidos = contar(tickets, EstadoTicket.VENDIDO);
        long cancelados = contar(tickets, EstadoTicket.CANCELADO);
        Map<String, Long> porTipo = tickets.stream()
                .collect(Collectors.groupingBy(Ticket::getTipoEntrada, Collectors.counting()));
        return new EstadisticasEvento(eventoId, tickets.size(), reservados, vendidos, cancelados,
                sumar(tickets, t -> t.getEstado() == EstadoTicket.VENDIDO),
                sumar(tickets, t -> t.getEstado() == EstadoTicket.RESERVADO),
                porTipo);
    }

    // misc
    private void escribirPdfEnEfs(Ticket t) {
        byte[] data = pdf.generar(t);
        Path p = efs.guardar(t.getEventoId(), t.getUsuarioId(), t.getCodigo(), data);
        t.setEfsPath(p.toString());
    }

    private void subirAS3Interno(Ticket t) {
        if (!efs.existe(t.getEfsPath())) {
            escribirPdfEnEfs(t);
        }
        byte[] data = efs.leer(t.getEfsPath());
        String key = t.getEventoId() + "/" + t.getUsuarioId() + "/" + nombreArchivo(t);
        s3.subir(key, data);
        t.setS3Key(key);
        if (eliminarEfsTrasSubir) {
            efs.eliminarSiExiste(t.getEfsPath());
            t.setEfsPath(null);
        }
    }

    private byte[] zip(List<Ticket> tickets) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ZipOutputStream z = new ZipOutputStream(bos)) {
            for (Ticket t : tickets) {
                z.putNextEntry(new ZipEntry(t.getEventoId() + "/" + t.getUsuarioId() + "/" + nombreArchivo(t)));
                z.write(descargar(t));
                z.closeEntry();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bos.toByteArray();
    }

    private List<Ticket> noVacia(List<Ticket> lista, String mensaje) {
        if (lista.isEmpty()) throw new TicketNoEncontradoException(mensaje);
        return lista;
    }

    private long contar(List<Ticket> tickets, EstadoTicket estado) {
        return tickets.stream().filter(t -> t.getEstado() == estado).count();
    }

    private BigDecimal sumar(List<Ticket> tickets, Predicate<Ticket> filtro) {
        return tickets.stream().filter(filtro).map(Ticket::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
