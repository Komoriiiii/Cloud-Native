package cl.duoc.tickets.dto;

import java.math.BigDecimal;
import java.util.Map;

public record EstadisticasEvento(
        String eventoId,
        long totalTickets,
        long reservados,
        long vendidos,
        long cancelados,
        BigDecimal ingresosVentas,
        BigDecimal ingresosPendientesReservas,
        Map<String, Long> ticketsPorTipo) {
}
