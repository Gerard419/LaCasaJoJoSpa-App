package cl.lacasajojo.usuarios;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Los nombres de los campos coinciden con el modelo Usuario de la app (Gson <-> Jackson)
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @NotBlank
    @Size(max = 60)
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Correo inválido")
    public String email;

    @NotBlank
    @Size(min = 2, max = 40)
    public String nombre;

    @NotBlank
    @Column(name = "password_hash")
    public String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    public Rol rol;

    public boolean activo = true;

    public String telefono;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String passwordHash, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }
}
