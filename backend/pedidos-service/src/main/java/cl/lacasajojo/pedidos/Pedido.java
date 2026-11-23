package cl.lacasajojo.pedidos;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
public class Pedido {

    // N° de boleta: lo asigna la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer numero;

    @NotBlank
    public String emailCliente;

    @NotBlank
    public String nombreCliente;

    @NotBlank
    public String fecha;

    @Valid
    @NotEmpty
    @Convert(converter = ItemsConverter.class)
    @Column(name = "items_json", length = 20000)
    public List<ItemPedido> items = new ArrayList<>();

    @Size(max = 200)
    public String observaciones = "";

    @Enumerated(EnumType.STRING)
    public EstadoPedido estado = EstadoPedido.RECIBIDO;

    public Pedido() {
    }
}
