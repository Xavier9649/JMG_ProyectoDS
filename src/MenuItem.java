import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

public class MenuItem {
    private Integer id;          // id_item
    private String nombre;
    private BigDecimal precio;
    private String categoria;    // puede ser null

    public MenuItem(Integer id, String nombre, BigDecimal precio, String categoria) {
        this.id = id;
        setNombre(nombre);
        setPrecio(precio);
        this.categoria = (categoria == null || categoria.isBlank()) ? null : categoria.trim();
    }

    public MenuItem(String nombre, BigDecimal precio, String categoria) {
        this(null, nombre, precio, categoria);
    }

    public MenuItem(String nombre, BigDecimal precio) {
        this(null, nombre, precio, null);
    }

    public Integer getId() { return id; }
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
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = (categoria == null || categoria.isBlank()) ? null : categoria.trim(); }

    @Override public String toString() { return nombre + " ($" + precio + ")"; }

    public void guardar() {
        final String sql = "INSERT INTO menu_item (nombre, precio, categoria) VALUES (?, ?, ?)";
        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setBigDecimal(2, precio);
            ps.setString(3, categoria);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) this.id = rs.getInt(1); }
        } catch (SQLException e) { throw new RuntimeException("Error al guardar MenuItem", e); }
    }

    public static Optional<MenuItem> buscarPorId(int id) {
        final String sql = "SELECT id_item AS id, nombre, precio, categoria FROM menu_item WHERE id_item=?";
        try (Connection c = ConexionDB.conectar();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new MenuItem(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio"),
                        rs.getString("categoria")
                ));
            }
        } catch (SQLException e) { throw new RuntimeException("Error al buscar MenuItem", e); }
    }
}

