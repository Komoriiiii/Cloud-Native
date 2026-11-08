package cl.duoc.tickets.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String eventoId;
    @Column(nullable = false) private String usuarioId;
    @Column(nullable = false, unique = true) private String codigo;
    @Column(nullable = false) private String nombreAsistente;
    @Column(nullable = false) private String tipoEntrada;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTicket estado;

    /** Ruta del PDF dentro del EFS (null si ya se elimino tras subirlo a S3). */
    private String efsPath;
    /** Clave del objeto en S3, ej: evento123/usuarioX/ticket-AB12CD34.pdf */
    private String s3Key;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    @PrePersist
    void alCrear() { creadoEn = LocalDateTime.now(); actualizadoEn = creadoEn; }

    @PreUpdate
    void alActualizar() { actualizadoEn = LocalDateTime.now(); }

    public Long getId() { return id; }
    public String getEventoId() { return eventoId; }
    public void setEventoId(String eventoId) { this.eventoId = eventoId; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombreAsistente() { return nombreAsistente; }
    public void setNombreAsistente(String nombreAsistente) { this.nombreAsistente = nombreAsistente; }
    public String getTipoEntrada() { return tipoEntrada; }
    public void setTipoEntrada(String tipoEntrada) { this.tipoEntrada = tipoEntrada; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public EstadoTicket getEstado() { return estado; }
    public void setEstado(EstadoTicket estado) { this.estado = estado; }
    public String getEfsPath() { return efsPath; }
    public void setEfsPath(String efsPath) { this.efsPath = efsPath; }
    public String getS3Key() { return s3Key; }
    public void setS3Key(String s3Key) { this.s3Key = s3Key; }
    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
