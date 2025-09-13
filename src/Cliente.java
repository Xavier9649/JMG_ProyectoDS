import java.sql.*;
import java.util.Optional;

public class Cliente {
    private Integer id;
    private String nombre;
    private String email;    // mapeado a columna 'correo'
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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        this.nombre = nombre.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = (email == null || email.isBlank()) ? null : email.trim();
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) {
        this.telefono = (telefono == null || telefono.isBlank()) ? null : telefono.trim();
    }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nombre='" + nombre + "', email='" + email + "', telefono='" + telefono + "'}";
    }

    // INSERT (usa columna 'correo')
    public void guardar() {
        final String sql = "INSERT INTO cliente (nombre, correo, telefono) VALUES (?, ?, ?)";
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

    // UPDATE (usa PK id_cliente y columna 'correo')
    public void actualizar() {
        if (id == null) throw new IllegalStateException("Cliente sin ID, no se puede actualizar");
        final String sql = "UPDATE cliente SET nombre=?, correo=?, telefono=? WHERE id_cliente=?";
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

    // DELETE (usa PK id_cliente)
    public void eliminar() {
        if (id == null) throw new IllegalStateException("Cliente sin ID, no se puede eliminar");
        final String sql = "DELETE FROM cliente WHERE id_cliente=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cliente", e);
        }
    }

    // SELECT por PK (alias para mantener getters iguales)
    public static Optional<Cliente> buscarPorId(int id) {
        final String sql = "SELECT id_cliente AS id, nombre, correo AS email, telefono FROM cliente WHERE id_cliente=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Cliente c = new Cliente(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getString("telefono")
                );
                return Optional.of(c);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cliente", e);
        }
    }
}

