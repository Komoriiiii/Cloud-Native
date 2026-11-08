package cl.duoc.tickets.repository;

import cl.duoc.tickets.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByEventoId(String eventoId);
    List<Ticket> findByEventoIdAndUsuarioId(String eventoId, String usuarioId);
}
