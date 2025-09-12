import java.sql.*;
import java.util.Optional;

public class Cliente {
    private Integer id;         // corresponde a id_cliente en la BD
    private String nombre;
    private String correo;      // 🔹 ahora se llama correo
    private String telefono;

    public Cliente(Integer id, String nombre, String correo, String telefono) {
        setId(id);
        setNombre(nombre);
        setCorreo(correo);
        setTelefono(telefono);
    }

    public Cliente(String nombre, String correo, String telefono) {
        this(null, nombre, correo, telefono);
    }

    // Encapsulamiento + validaciones simples
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = nombre.trim();
    }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) {
        this.correo = (correo == null) ? null : correo.trim();
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
        final String sql = "INSERT INTO cliente (nombre, correo, telefono) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, telefono);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) this.id = rs.getInt(1); // devuelve id_cliente autoincrement
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar cliente", e);
        }
    }

    public void actualizar() {
        if (id == null) throw new IllegalStateException("Cliente sin id");
        final String sql = "UPDATE cliente SET nombre=?, correo=?, telefono=? WHERE id_cliente=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, telefono);
            ps.setInt(4, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cliente", e);
        }
    }

    public void eliminar() {
        if (id == null) throw new IllegalStateException("Cliente sin id");
        final String sql = "DELETE FROM cliente WHERE id_cliente=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    public static Optional<Cliente> buscarPorId(int id) {
        final String sql = "SELECT id_cliente, nombre, correo, telefono FROM cliente WHERE id_cliente=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Cliente(
                        rs.getInt("id_cliente"),
                        rs.getString("nombre"),
                        rs.getString("correo"),
                        rs.getString("telefono")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }
}
