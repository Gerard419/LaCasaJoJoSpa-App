package cl.lacasajojo.usuarios;

public record LoginRequest(String email, String passwordHash) {
}
