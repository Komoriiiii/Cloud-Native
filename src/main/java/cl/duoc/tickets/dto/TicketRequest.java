package cl.duoc.tickets.dto;

import cl.duoc.tickets.model.EstadoTicket;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record TicketRequest(
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "eventoId solo admite letras, numeros, - y _")
        String eventoId,
        @NotBlank @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "usuarioId solo admite letras, numeros, - y _")
        String usuarioId,
        @NotBlank String nombreAsistente,
        @NotBlank String tipoEntrada,
        @NotNull @DecimalMin("0.0") BigDecimal precio,
        EstadoTicket estado) {
}
