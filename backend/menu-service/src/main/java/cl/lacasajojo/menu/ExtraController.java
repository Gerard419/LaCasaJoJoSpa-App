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
@RequestMapping("/api/extras")
public class ExtraController {

    private final ExtraRepository repo;

    public ExtraController(ExtraRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Extra> listar() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<Extra> crear(@Valid @RequestBody Extra extra) {
        extra.id = null;
        extra.nombre = extra.nombre.trim();
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(extra));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Extra> actualizar(@PathVariable Integer id, @Valid @RequestBody Extra extra) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        extra.id = id;
        extra.nombre = extra.nombre.trim();
        return ResponseEntity.ok(repo.save(extra));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
