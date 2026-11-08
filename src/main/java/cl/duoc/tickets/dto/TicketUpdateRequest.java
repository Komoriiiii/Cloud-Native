package cl.duoc.tickets.dto;

import cl.duoc.tickets.model.EstadoTicket;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/** todos los campos son opcionales */
public record TicketUpdateRequest(
        String nombreAsistente,
        String tipoEntrada,
        @DecimalMin("0.0") BigDecimal precio,
        EstadoTicket estado) {
}
