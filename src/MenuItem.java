import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class MenuItem {
    private Integer id;
    private String nombre;
    private BigDecimal precio; // dinero → BigDecimal
    private boolean activo = true;

    public MenuItem(Integer id, String nombre, BigDecimal precio, boolean activo) {
        setId(id);
        setNombre(nombre);
        setPrecio(precio);
        setActivo(activo);
    }

    public MenuItem(String nombre, BigDecimal precio) {
        this(null, nombre, precio, true);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre requerido");
        this.nombre = nombre.trim();
    }

    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) {
        if (precio == null || precio.signum() < 0) throw new IllegalArgumentException("Precio inválido");
        this.precio = precio;
    }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    @Override
    public String toString() {
        return "MenuItem{id=%s, nombre='%s', precio=%s}".formatted(id, nombre, precio);
    }

    // CRUD básico
    public void guardar() {
        final String sql = "INSERT INTO menu_item (nombre, precio, activo) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setBigDecimal(2, precio);
            ps.setBoolean(3, activo);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) this.id = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar producto", e);
        }
    }

    public static Optional<MenuItem> buscarPorId(int id) {
        final String sql = "SELECT id, nombre, precio, activo FROM menu_item WHERE id=?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio"),
                        rs.getBoolean("activo")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar producto", e);
        }
    }
}
