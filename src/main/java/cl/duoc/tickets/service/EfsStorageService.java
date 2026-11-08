package cl.duoc.tickets.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Almacenamiento temporal de los tickets en el sistema de archivos montado desde EFS. */
@Service
public class EfsStorageService {

    private final Path base;

    public EfsStorageService(@Value("${app.efs.path}") String efsPath) {
        this.base = Paths.get(efsPath).toAbsolutePath().normalize();
    }

    public Path guardar(String eventoId, String usuarioId, String codigo, byte[] contenido) {
        try {
            Path destino = base.resolve(eventoId).resolve(usuarioId).resolve("ticket-" + codigo + ".pdf").normalize();
            if (!destino.startsWith(base)) {
                throw new IllegalArgumentException("Ruta invalida");
            }
            Files.createDirectories(destino.getParent());
            Files.write(destino, contenido);
            return destino;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean existe(String ruta) {
        return ruta != null && Files.exists(Paths.get(ruta));
    }

    public byte[] leer(String ruta) {
        try {
            return Files.readAllBytes(Paths.get(ruta));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void eliminarSiExiste(String ruta) {
        if (ruta == null) return;
        try {
            Files.deleteIfExists(Paths.get(ruta));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
