import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private Integer id;                 // id_pedido
    private Cliente cliente;            // id_cliente
    private Integer idMesero;           // NOT NULL en tu BD
    private String numeroMesa;          // VARCHAR(10) o null
    private final List<DetallePedido> detalles = new ArrayList<>();
    private EstadoPedido estado = EstadoPedido.PENDIENTE;
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    public Pedido(Cliente cliente, Integer idMesero, String numeroMesa) {
        if (cliente == null || cliente.getId() == null) throw new IllegalArgumentException("Cliente válido requerido");
        if (idMesero == null) throw new IllegalArgumentException("idMesero es obligatorio");
        this.cliente = cliente;
        this.idMesero = idMesero;
        this.numeroMesa = (numeroMesa == null || numeroMesa.isBlank()) ? null : numeroMesa.trim();
    }

    public Integer getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public Integer getIdMesero() { return idMesero; }
    public String getNumeroMesa() { return numeroMesa; }
    public EstadoPedido getEstado() { return estado; }
    public List<DetallePedido> getDetalles() { return List.copyOf(detalles); }

    public void agregarDetalle(MenuItem item, int cantidad) {
        detalles.add(new DetallePedido(item, cantidad));
    }

    public BigDecimal calcularTotal() {
        return detalles.stream().map(DetallePedido::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void cambiarEstado(EstadoPedido nuevo) {
        if (!estado.puedeTransitarA(nuevo)) throw new IllegalStateException("Transición inválida: " + estado + " → " + nuevo);
        this.estado = nuevo;
    }

    // INSERT en pedido + detalles (usa precio_unitario en detalles; total en cabecera)
    public void guardar() {
        if (detalles.isEmpty()) throw new IllegalStateException("Pedido sin detalles");

        final String sqlPedido =
                "INSERT INTO pedido (id_cliente, id_mesero, estado, numero_mesa, total) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionDB.conectar()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, cliente.getId());
                ps.setInt(2, idMesero);
                ps.setString(3, estado.toDb());        // 'pendiente'/'listo'
                ps.setString(4, numeroMesa);
                ps.setBigDecimal(5, calcularTotal());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) this.id = rs.getInt(1);
                }
            }

            for (DetallePedido d : detalles) {
                d.setIdPedido(this.id);
                d.guardarCon(conn);
            }

            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("Error al guardar pedido", e);
        }
    }

    public void actualizarEstado(EstadoPedido nuevo) {
        cambiarEstado(nuevo);
        final String sql = "UPDATE pedido SET estado=? WHERE id_pedido=?";
        try (Connection conn =ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado.toDb());
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar estado", e);
        }
    }
}

