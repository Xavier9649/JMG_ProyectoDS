import java.sql.*;
import java.util.Optional;

public class Cliente {
    private Integer id;         // puede ser autoincrement
    private String nombre;
    private String email;
    private String telefono;

    public Cliente(Integer id, String nombre, String email, String telefono) {
        setId(id);
        setNombre(nombre);
        setEmail(email);
        setTelefono(telefono);
    }

    public Cliente(String nombre, String email, String telefono) {
        this(null, nombre, email, telefono);
    }

    // Encapsulamiento + validaciones simples
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = nombre.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = (email == null) ? null : email.trim();
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) {
        this.telefono = (telefono == null) ? null : telefono.trim();
    }

    @Override
    public String toString() {
        return "Cliente{id=%s, nombre='%s'}".formatted(id, nombre);
    }

    // ===== CRUD (mínimo) =====
    public void guardar() {
        final String sql = "INSERT INTO cliente (nombre, email, telefono) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, telefono);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) this.id = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente", e);
        }
    }

    public void actualizar() {
        if (id == null) throw new IllegalStateException("Cliente sin id");
        final String sql = "UPDATE cliente SET nombre=?, email=?, telefono=? WHERE id=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, telefono);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
    }

    public void eliminar() {
        if (id == null) throw new IllegalStateException("Cliente sin id");
        final String sql = "DELETE FROM cliente WHERE id=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    public static Optional<Cliente> buscarPorId(int id) {
        final String sql = "SELECT id, nombre, email, telefono FROM cliente WHERE id=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("telefono")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }
}

