package cl.lacasajojo.usuarios;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private static final String HEADER_ADMIN = "X-Admin-Email";

    private final UsuarioRepository repo;

    public UsuarioController(UsuarioRepository repo) {
        this.repo = repo;
    }

    // Solo el administrador puede ver la lista completa
    @GetMapping
    public ResponseEntity<List<Usuario>> listar(@RequestHeader(value = HEADER_ADMIN, required = false) String adminEmail) {
        if (!esAdminActivo(adminEmail)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(repo.findAll());
    }

    // 200 con el usuario, 401 credenciales incorrectas, 403 cuenta bloqueada
    @PostMapping("/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest req) {
        if (req == null || req.email() == null || req.passwordHash() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return repo.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> u.passwordHash.equals(req.passwordHash()))
                .map(u -> u.activo
                        ? ResponseEntity.ok(u)
                        : ResponseEntity.status(HttpStatus.FORBIDDEN).<Usuario>build())
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // Registro público = solo clientes. Crear dueño/admin exige el encabezado de un administrador activo.
    @PostMapping
    public ResponseEntity<Usuario> crear(
            @Valid @RequestBody Usuario nuevo,
            @RequestHeader(value = HEADER_ADMIN, required = false) String adminEmail) {
        if (nuevo.rol != Rol.CLIENTE && !esAdminActivo(adminEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (repo.findByEmailIgnoreCase(nuevo.email.trim()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        nuevo.email = nuevo.email.trim();
        nuevo.nombre = nuevo.nombre.trim();
        nuevo.activo = true;
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(nuevo));
    }

    // Bloquear / activar usuario (solo administrador)
    @PutMapping("/estado")
    public ResponseEntity<Usuario> cambiarEstado(
            @RequestParam String email,
            @RequestParam boolean activo,
            @RequestHeader(value = HEADER_ADMIN, required = false) String adminEmail) {
        if (!esAdminActivo(adminEmail)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return repo.findByEmailIgnoreCase(email)
                .map(u -> {
                    u.activo = activo;
                    return ResponseEntity.ok(repo.save(u));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private boolean esAdminActivo(String email) {
        if (email == null) return false;
        return repo.findByEmailIgnoreCase(email.trim())
                .map(u -> u.rol == Rol.ADMIN && u.activo)
                .orElse(false);
    }
}
