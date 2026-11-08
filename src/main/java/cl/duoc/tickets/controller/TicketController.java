package cl.duoc.tickets.controller;

import cl.duoc.tickets.dto.EstadisticasEvento;
import cl.duoc.tickets.dto.TicketRequest;
import cl.duoc.tickets.dto.TicketUpdateRequest;
import cl.duoc.tickets.model.Ticket;
import cl.duoc.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    /** generar ticket (pdf en efs) lo sube a s3 automaticamente. */
    @PostMapping("/tickets")
    public ResponseEntity<Ticket> generar(@Valid @RequestBody TicketRequest request,
                                          @RequestParam(defaultValue = "true") boolean subirS3) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generar(request, subirS3));
    }

    /** subir (o resubir) un ticket existente a s3 */
    @PostMapping("/tickets/{id}/subir")
    public Ticket subir(@PathVariable Long id) {
        return service.subirAS3(id);
    }

    @GetMapping("/tickets/{id}")
    public Ticket obtener(@PathVariable Long id) {
        return service.buscar(id);
    }

    @GetMapping("/tickets/evento/{eventoId}")
    public List<Ticket> listarPorEvento(@PathVariable String eventoId) {
        return service.listarPorEvento(eventoId);
    }

    /** descargar un ticket (pdf) */
    @GetMapping("/tickets/{id}/descargar")
    public ResponseEntity<byte[]> descargar(@PathVariable Long id) {
        Ticket t = service.buscar(id);
        return archivo(service.descargar(t), service.nombreArchivo(t), MediaType.APPLICATION_PDF);
    }

    /** descargar todos los tickets de un evento (zip) */
    @GetMapping("/tickets/evento/{eventoId}/descargar")
    public ResponseEntity<byte[]> descargarPorEvento(@PathVariable String eventoId) {
        return archivo(service.descargarZipPorEvento(eventoId), eventoId + ".zip", MediaType.parseMediaType("application/zip"));
    }

    /** descargar los tickets de un usuario dentro de un evento (zip) */
    @GetMapping("/tickets/evento/{eventoId}/usuario/{usuarioId}/descargar")
    public ResponseEntity<byte[]> descargarPorUsuario(@PathVariable String eventoId, @PathVariable String usuarioId) {
        return archivo(service.descargarZipPorUsuario(eventoId, usuarioId), eventoId + "-" + usuarioId + ".zip",
                MediaType.parseMediaType("application/zip"));
    }

    /** modificar detalles del ticket */
    @PutMapping("/tickets/{id}")
    public Ticket actualizar(@PathVariable Long id, @Valid @RequestBody TicketUpdateRequest request) {
        return service.actualizar(id, request);
    }

    /**  eliminar ticket (BD, EFS y S3) */
    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    /** estadisticas de ventas y reservas por evento */
    @GetMapping("/eventos/{eventoId}/estadisticas")
    public EstadisticasEvento estadisticas(@PathVariable String eventoId) {
        return service.estadisticas(eventoId);
    }

    private ResponseEntity<byte[]> archivo(byte[] contenido, String nombre, MediaType tipo) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(tipo);
        h.setContentDisposition(ContentDisposition.attachment().filename(nombre).build());
        return new ResponseEntity<>(contenido, h, HttpStatus.OK);
    }
}
