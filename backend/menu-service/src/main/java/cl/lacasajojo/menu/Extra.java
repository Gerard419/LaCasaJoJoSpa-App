package cl.lacasajojo.menu;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "extras")
public class Extra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @NotBlank
    @Size(min = 3, max = 40)
    public String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    public TipoExtra tipo;

    public boolean disponible = true;

    public Extra() {
    }

    public Extra(String nombre, TipoExtra tipo) {
        this.nombre = nombre;
        this.tipo = tipo;
    }
}
