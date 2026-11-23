package cl.lacasajojo.menu;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platos")
public class PlatoController {

    private final PlatoRepository repo;

    public PlatoController(PlatoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Plato> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plato> obtener(@PathVariable Integer id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // El id lo asigna la base de datos (la app manda id = 0)
    @PostMapping
    public ResponseEntity<Plato> crear(@Valid @RequestBody Plato plato) {
        plato.id = null;
        plato.nombre = plato.nombre.trim();
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(plato));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Plato> actualizar(@PathVariable Integer id, @Valid @RequestBody Plato plato) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        plato.id = id;
        plato.nombre = plato.nombre.trim();
        return ResponseEntity.ok(repo.save(plato));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
