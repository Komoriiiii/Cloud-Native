package cl.duoc.tickets.service;

import cl.duoc.tickets.model.Ticket;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generar(Ticket t) {
        Document doc = new Document(PageSize.A5);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
        Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font texto = FontFactory.getFont(FontFactory.HELVETICA, 12);

        doc.add(new Paragraph("TICKET DE EVENTO", titulo));
        doc.add(new Paragraph(" "));
        linea(doc, "Codigo: ", t.getCodigo(), etiqueta, texto);
        linea(doc, "Evento: ", t.getEventoId(), etiqueta, texto);
        linea(doc, "Usuario: ", t.getUsuarioId(), etiqueta, texto);
        linea(doc, "Asistente: ", t.getNombreAsistente(), etiqueta, texto);
        linea(doc, "Tipo de entrada: ", t.getTipoEntrada(), etiqueta, texto);
        linea(doc, "Precio: ", "$" + t.getPrecio().toPlainString(), etiqueta, texto);
        linea(doc, "Estado: ", t.getEstado().name(), etiqueta, texto);

        doc.close();
        return out.toByteArray();
    }

    private void linea(Document doc, String etiqueta, String valor, Font fe, Font fv) {
        Paragraph p = new Paragraph();
        p.add(new com.lowagie.text.Chunk(etiqueta, fe));
        p.add(new com.lowagie.text.Chunk(valor, fv));
        doc.add(p);
    }
}
