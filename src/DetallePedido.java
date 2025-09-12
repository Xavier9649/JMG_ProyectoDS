import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class DetallePedido {
    private Integer id;
    private Integer idPedido;
    private MenuItem item;
    private int cantidad;

    public DetallePedido(Integer id, Integer idPedido, MenuItem item, int cantidad) {
        setId(id);
        setIdPedido(idPedido);
        setItem(item);
        setCantidad(cantidad);
    }

    public DetallePedido(MenuItem item, int cantidad) {
        this(null, null, item, cantidad);
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public MenuItem getItem() { return item; }
    public void setItem(MenuItem item) {
        if (item == null) throw new IllegalArgumentException("Item requerido");
        this.item = item;
    }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad > 0");
        this.cantidad = cantidad;
    }

    public BigDecimal getSubtotal() {
        return item.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }

    // Persistencia interna usada por Pedido.guardar()
    void guardarCon(Connection conn) throws SQLException {
        final String sql = "INSERT INTO detalle_pedido (id_pedido, id_menu_item, cantidad, subtotal) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, item.getId());
            ps.setInt(3, cantidad);
            ps.setBigDecimal(4, getSubtotal());
            ps.executeUpdate();
        }
    }
}
