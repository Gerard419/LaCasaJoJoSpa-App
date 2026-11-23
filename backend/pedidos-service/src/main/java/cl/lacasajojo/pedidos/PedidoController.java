package cl.lacasajojo.pedidos;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoRepository repo;

    public PedidoController(PedidoRepository repo) {
        this.repo = repo;
    }

    // Sin parámetro: todos (dueño). Con ?email=: solo los de ese cliente.
    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) String email) {
        if (email == null || email.isBlank()) return repo.findAllByOrderByNumeroDesc();
        return repo.findByEmailClienteIgnoreCaseOrderByNumeroDesc(email.trim());
    }

    @GetMapping("/{numero}")
    public ResponseEntity<Pedido> obtener(@PathVariable Integer numero) {
        return repo.findById(numero).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Todo pedido nace en estado RECIBIDO y con el número que entrega la BD
    @PostMapping
    public ResponseEntity<Pedido> crear(@Valid @RequestBody Pedido pedido) {
        pedido.numero = null;
        pedido.estado = EstadoPedido.RECIBIDO;
        if (pedido.observaciones == null) pedido.observaciones = "";
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(pedido));
    }

    @PutMapping("/{numero}/estado")
    public ResponseEntity<Pedido> cambiarEstado(@PathVariable Integer numero, @RequestParam EstadoPedido estado) {
        return repo.findById(numero)
                .map(p -> {
                    p.estado = estado;
                    return ResponseEntity.ok(repo.save(p));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
