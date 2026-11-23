package cl.lacasajojo.menu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Guarda las fotos dentro del servidor del menú. Así las ven dueño y cliente desde
 * distintos teléfonos; no se guardan rutas del almacenamiento privado de Android.
 * Las fotos se guardan en JPEG con nombres UUID, nunca con el nombre recibido.
 * Solo es apto para una demostración en red de laboratorio; falta autenticación
 * antes de exponerlo a Internet.
 */
@RestController
@RequestMapping("/api/fotos")
public class FotoController {

    private final Path directorio;

    public FotoController(@Value("${lacasajojo.fotos.directorio:./data/fotos}") String directorio) {
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> subir(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty() || archivo.getSize() > 3_000_000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Imagen vacía o demasiado grande"));
        }
        // Verificamos el contenido; no confiamos en la extensión ni el tipo MIME enviado.
        BufferedImage imagen;
        try (var stream = archivo.getInputStream()) {
            imagen = ImageIO.read(stream);
        }
        if (imagen == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Archivo no es una imagen"));
        }
        Files.createDirectories(directorio);
        var nombre = UUID.randomUUID() + ".jpg";
        var destino = directorio.resolve(nombre);
        if (!ImageIO.write(imagen, "jpg", destino.toFile())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ruta", "/api/fotos/" + nombre));
    }

    @GetMapping(value = "/{nombre}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> ver(@PathVariable String nombre) throws IOException {
        if (!nombre.matches("[0-9a-fA-F-]{36}\\.jpg")) return ResponseEntity.badRequest().build();
        var ruta = directorio.resolve(nombre);
        if (!Files.isRegularFile(ruta)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(Files.readAllBytes(ruta));
    }
}
