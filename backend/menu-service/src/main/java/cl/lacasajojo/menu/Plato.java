package cl.lacasajojo.menu;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "platos")
public class Plato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @NotBlank
    @Size(min = 3, max = 40)
    public String nombre;

    @Min(1)
    @Max(100000)
    public int precio;

    public boolean disponible = true;

    // Ruta compartida en menu-service: /api/fotos/UUID.jpg (no ruta privada del celular)
    public String foto;

    public Plato() {
    }

    public Plato(String nombre, int precio) {
        this.nombre = nombre;
        this.precio = precio;
    }
}
