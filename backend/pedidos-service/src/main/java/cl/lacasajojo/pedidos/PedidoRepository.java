package cl.lacasajojo.pedidos;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByEmailClienteIgnoreCaseOrderByNumeroDesc(String emailCliente);

    List<Pedido> findAllByOrderByNumeroDesc();
}
