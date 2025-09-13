import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DetallePedido {
    private Integer idDetalle;
    private Integer idPedido;        // lo setea Pedido.guardar()
    private MenuItem item;           // id_item en BD
    private int cantidad;
    private BigDecimal precioUnit;   // se toma del MenuItem al crear el detalle

    public DetallePedido(MenuItem item, int cantidad) {
        if (item == null || item.getId() == null) throw new IllegalArgumentException("MenuItem inválido");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad > 0");
        this.item = item;
        this.cantidad = cantidad;
        this.precioUnit = item.getPrecio();
    }

    public Integer getIdPedido() { return idPedido; }
    public MenuItem getItem() { return item; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnit() { return precioUnit; }
    public BigDecimal getSubtotal() { return precioUnit.multiply(BigDecimal.valueOf(cantidad)); }

    void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    // Inserta en detalle_pedido (id_item, cantidad, precio_unitario)
    void guardarCon(Connection conn) throws SQLException {
        final String sql = "INSERT INTO detalle_pedido (id_pedido, id_item, cantidad, precio_unitario) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setInt(2, item.getId());
            ps.setInt(3, cantidad);
            ps.setBigDecimal(4, precioUnit);
            ps.executeUpdate();
        }
    }
}

