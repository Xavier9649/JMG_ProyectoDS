import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private Integer idPedido; // cambiado de 'id' a 'idPedido'
    private Cliente cliente;
    private final List<DetallePedido> detalles = new ArrayList<>();
    private EstadoPedido estado = EstadoPedido.REGISTRADO;
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Pedido(Cliente cliente) {
        setCliente(cliente);
    }

    public Integer getId() { return idPedido; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) {
        if (cliente == null || cliente.getId() == null)
            throw new IllegalArgumentException("Cliente válido requerido");
        this.cliente = cliente;
    }
    public List<DetallePedido> getDetalles() { return List.copyOf(detalles); }
    public EstadoPedido getEstado() { return estado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void agregarDetalle(MenuItem item, int cantidad) {
        if (item.getId() == null) throw new IllegalArgumentException("MenuItem debe estar guardado en BD");
        detalles.add(new DetallePedido(item, cantidad));
    }

    public BigDecimal calcularTotal() {
        return detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cambiarEstado(EstadoPedido nuevo) {
        if (!estado.puedeTransitarA(nuevo))
            throw new IllegalStateException("Transición inválida: " + estado + " → " + nuevo);
        this.estado = nuevo;
    }

    // ===== Persistencia con transacción =====
    public void guardar() {
        if (detalles.isEmpty()) throw new IllegalStateException("Pedido sin detalles");
        final String sqlPedido = "INSERT INTO pedido (id_cliente, estado, total, fecha_creacion) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionDB.conectar()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cliente.getId());
                ps.setString(2, estado.name());
                ps.setBigDecimal(3, calcularTotal());
                ps.setTimestamp(4, Timestamp.valueOf(fechaCreacion));
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.idPedido = rs.getInt(1);
                }
            }

            // Inserta detalles
            for (DetallePedido d : detalles) {
                d.setIdPedido(this.idPedido);
                d.guardarCon(conn);
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar pedido", e);
        }
    }

    public void actualizarEstado(EstadoPedido nuevo) {
        cambiarEstado(nuevo);
        final String sql = "UPDATE pedido SET estado=? WHERE id_pedido=?"; // corregido
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado", e);
        }
    }
}
