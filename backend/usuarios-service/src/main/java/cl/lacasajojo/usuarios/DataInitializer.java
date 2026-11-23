package cl.lacasajojo.usuarios;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Cuentas de prueba (las mismas del README). La app envía la contraseña como SHA-256.
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner sembrarUsuarios(UsuarioRepository repo) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(new Usuario("Administrador", "admin@lacasajojo.cl", sha256("Admin123"), Rol.ADMIN));
            repo.save(new Usuario("Dueño", "dueno@lacasajojo.cl", sha256("Dueno123"), Rol.DUENO));
        };
    }

    static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
