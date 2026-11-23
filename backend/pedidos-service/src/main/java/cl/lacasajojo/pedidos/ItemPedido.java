package cl.lacasajojo.pedidos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class ItemPedido {

    @NotBlank
    public String plato;

    @Min(1)
    public int precioUnitario;

    public List<String> agregados = new ArrayList<>();

    @NotBlank
    public String ensalada;

    @Min(1)
    public int cantidad = 1;

    public ItemPedido() {
    }
}
