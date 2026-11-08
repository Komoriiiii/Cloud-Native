package cl.duoc.tickets.dto;

import cl.duoc.tickets.model.EstadoTicket;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/** Todos los campos son opcionales: solo se modifican los que vienen informados. */
public record TicketUpdateRequest(
        String nombreAsistente,
        String tipoEntrada,
        @DecimalMin("0.0") BigDecimal precio,
        EstadoTicket estado) {
}
