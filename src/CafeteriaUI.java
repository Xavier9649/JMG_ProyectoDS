import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * UI Swing minimal para el proyecto de cafetería.
 *
 * Requisitos previos: tus clases existentes en el mismo src/ sin package:
 *  - ConexionBD (JDBC a MySQL)
 *  - Cliente (CRUD básico con guardar/actualizar/eliminar/buscarPorId)
 *  - MenuItem (guardar/buscarPorId)
 *  - Pedido (agregarDetalle, calcularTotal, guardar, actualizarEstado)
 *  - DetallePedido (usada por Pedido)
 *  - EstadoPedido (enum)
 *  - NotificacionService (impresión en consola)
 *
 * Esta UI agrega 3 pestañas:
 *  1) Clientes: Crear / Buscar / Actualizar / Eliminar
 *  2) Pedidos (Mesero): armar pedido con items existentes y guardarlo
 *  3) Cocina: ver pedidos y actualizar estados (EN_PREPARACION → LISTO)
 *
 *  Nota: Para simplificar y mantenerte en POO, toda la lógica de dominio
 *  usa las clases del modelo. Esta UI solo orquesta y renderiza tablas.
 */
public class CafeteriaUI extends JFrame {

    // ===== CLIENTES =====
    private JTextField txtCliId;
    private JTextField txtCliNombre;
    private JTextField txtCliEmail;
    private JTextField txtCliTelefono;
    private JTable tablaClientes;
    private DefaultTableModel modeloClientes;

    // ===== PEDIDOS (MESERO) =====
    private JTextField txtPedidoClienteId;
    private JTextField txtItemId;
    private JTextField txtCantidad;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel lblTotalPedido;
    private final List<DetallePedido> carritoDetalles = new ArrayList<>();

    // ===== COCINA =====
    private JTable tablaCocina;
    private DefaultTableModel modeloCocina;

    private final NotificacionService notificador = new NotificacionService();

    public CafeteriaUI() {
        super("Cafetería - Gestión (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 680);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", crearPanelClientes());
        tabs.addTab("Pedidos (Mesero)", crearPanelPedidos());
        tabs.addTab("Cocina", crearPanelCocina());
        add(tabs, BorderLayout.CENTER);

        // Cargar listados iniciales
        refrescarTablaClientes();
        refrescarTablaCocina();
    }

    // ===================== PANEL CLIENTES =====================
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout());

        // Formulario superior
        JPanel form = new JPanel(new GridLayout(2, 1));
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT));

        txtCliId = new JTextField(6);
        txtCliNombre = new JTextField(18);
        txtCliEmail = new JTextField(18);
        txtCliTelefono = new JTextField(12);

        fila1.add(new JLabel("ID:"));
        fila1.add(txtCliId);
        fila1.add(Box.createHorizontalStrut(10));
        fila1.add(new JLabel("Nombre:"));
        fila1.add(txtCliNombre);
        fila1.add(Box.createHorizontalStrut(10));
        fila1.add(new JLabel("Email:"));
        fila1.add(txtCliEmail);

        fila2.add(new JLabel("Teléfono:"));
        fila2.add(txtCliTelefono);
        fila2.add(Box.createHorizontalStrut(10));

        JButton btnGuardar = new JButton("Guardar");
        JButton btnBuscar = new JButton("Buscar por ID");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnRefrescar = new JButton("Refrescar");

        fila2.add(btnGuardar);
        fila2.add(btnBuscar);
        fila2.add(btnActualizar);
        fila2.add(btnEliminar);
        fila2.add(btnRefrescar);

        form.add(fila1);
        form.add(fila2);

        panel.add(form, BorderLayout.NORTH);

        // Tabla clientes
        modeloClientes = new DefaultTableModel(new Object[]{"ID", "Nombre", "Email", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaClientes = new JTable(modeloClientes);
        panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        // Listeners
        btnGuardar.addActionListener(this::accionGuardarCliente);
        btnBuscar.addActionListener(this::accionBuscarCliente);
        btnActualizar.addActionListener(this::accionActualizarCliente);
        btnEliminar.addActionListener(this::accionEliminarCliente);
        btnRefrescar.addActionListener(e -> refrescarTablaClientes());

        // Click en tabla para cargar al formulario
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaClientes.getSelectedRow() >= 0) {
                int row = tablaClientes.getSelectedRow();
                txtCliId.setText(String.valueOf(tablaClientes.getValueAt(row, 0)));
                txtCliNombre.setText(String.valueOf(tablaClientes.getValueAt(row, 1)));
                txtCliEmail.setText(String.valueOf(tablaClientes.getValueAt(row, 2)));
                txtCliTelefono.setText(String.valueOf(tablaClientes.getValueAt(row, 3)));
            }
        });

        return panel;
    }

    private void accionGuardarCliente(ActionEvent e) {
        try {
            String nombre = txtCliNombre.getText().trim();
            String email = txtCliEmail.getText().trim();
            String tel = txtCliTelefono.getText().trim();
            Cliente c = new Cliente(nombre, email.isBlank()? null: email, tel.isBlank()? null: tel);
            c.guardar();
            JOptionPane.showMessageDialog(this, "Cliente guardado con ID: " + c.getId());
            limpiarFormCliente();
            refrescarTablaClientes();
        } catch (Exception ex) {
            mostrarError("Guardar cliente", ex);
        }
    }

    private void accionBuscarCliente(ActionEvent e) {
        try {
            int id = Integer.parseInt(txtCliId.getText().trim());
            Optional<Cliente> oc = Cliente.buscarPorId(id);
            if (oc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No se encontró cliente");
                return;
            }
            Cliente c = oc.get();
            txtCliNombre.setText(c.getNombre());
            txtCliEmail.setText(c.getEmail());
            txtCliTelefono.setText(c.getTelefono());
        } catch (Exception ex) {
            mostrarError("Buscar cliente", ex);
        }
    }

    private void accionActualizarCliente(ActionEvent e) {
        try {
            int id = Integer.parseInt(txtCliId.getText().trim());
            Cliente c = new Cliente(id, txtCliNombre.getText().trim(),
                    txtCliEmail.getText().trim().isBlank()? null: txtCliEmail.getText().trim(),
                    txtCliTelefono.getText().trim().isBlank()? null: txtCliTelefono.getText().trim());
            c.actualizar();
            JOptionPane.showMessageDialog(this, "Cliente actualizado");
            limpiarFormCliente();
            refrescarTablaClientes();
        } catch (Exception ex) {
            mostrarError("Actualizar cliente", ex);
        }
    }

    private void accionEliminarCliente(ActionEvent e) {
        try {
            int id = Integer.parseInt(txtCliId.getText().trim());
            Cliente c = new Cliente(id, "tmp", null, null);
            c.eliminar();
            JOptionPane.showMessageDialog(this, "Cliente eliminado");
            limpiarFormCliente();
            refrescarTablaClientes();
        } catch (Exception ex) {
            mostrarError("Eliminar cliente", ex);
        }
    }

    private void limpiarFormCliente() {
        txtCliId.setText("");
        txtCliNombre.setText("");
        txtCliEmail.setText("");
        txtCliTelefono.setText("");
    }

    private void refrescarTablaClientes() {
        modeloClientes.setRowCount(0);
        final String sql = "SELECT id, nombre, email, telefono FROM cliente ORDER BY id DESC";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloClientes.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("nombre"), rs.getString("email"), rs.getString("telefono")
                });
            }
        } catch (SQLException e) {
            mostrarError("Cargar clientes", e);
        }
    }

    // ===================== PANEL PEDIDOS (MESERO) =====================
    private JPanel crearPanelPedidos() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPedidoClienteId = new JTextField(6);
        txtItemId = new JTextField(6);
        txtCantidad = new JTextField(4);
        JButton btnAddItem = new JButton("Agregar al carrito");
        JButton btnGuardarPedido = new JButton("Guardar Pedido");
        lblTotalPedido = new JLabel("Total: $0.00");

        top.add(new JLabel("ID Cliente:"));
        top.add(txtPedidoClienteId);
        top.add(Box.createHorizontalStrut(12));
        top.add(new JLabel("ID Item:"));
        top.add(txtItemId);
        top.add(new JLabel("Cant:"));
        top.add(txtCantidad);
        top.add(btnAddItem);
        top.add(Box.createHorizontalStrut(12));
        top.add(btnGuardarPedido);
        top.add(Box.createHorizontalStrut(12));
        top.add(lblTotalPedido);

        panel.add(top, BorderLayout.NORTH);

        modeloCarrito = new DefaultTableModel(new Object[]{"ID Item", "Nombre", "Precio", "Cantidad", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // acciones
        btnAddItem.addActionListener(this::accionAgregarItemCarrito);
        btnGuardarPedido.addActionListener(this::accionGuardarPedido);

        return panel;
    }

    private void accionAgregarItemCarrito(ActionEvent e) {
        try {
            int idItem = Integer.parseInt(txtItemId.getText().trim());
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) throw new IllegalArgumentException("Cantidad debe ser > 0");

            Optional<MenuItem> oi = MenuItem.buscarPorId(idItem);
            if (oi.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No existe MenuItem con id " + idItem);
                return;
            }
            MenuItem mi = oi.get();
            DetallePedido det = new DetallePedido(mi, cant);
            carritoDetalles.add(det);

            BigDecimal subtotal = det.getSubtotal();
            modeloCarrito.addRow(new Object[]{mi.getId(), mi.getNombre(), mi.getPrecio(), cant, subtotal});

            actualizarTotalCarrito();
            txtItemId.setText("");
            txtCantidad.setText("");
        } catch (Exception ex) {
            mostrarError("Agregar item", ex);
        }
    }

    private void actualizarTotalCarrito() {
        BigDecimal total = carritoDetalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalPedido.setText("Total: $" + total);
    }

    private void accionGuardarPedido(ActionEvent e) {
        try {
            int idCliente = Integer.parseInt(txtPedidoClienteId.getText().trim());
            Optional<Cliente> oc = Cliente.buscarPorId(idCliente);
            if (oc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cliente no existe");
                return;
            }
            if (carritoDetalles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrito vacío");
                return;
            }

            Cliente cliente = oc.get();
            Pedido p = new Pedido(cliente);
            for (DetallePedido d : carritoDetalles) {
                p.agregarDetalle(d.getItem(), d.getCantidad());
            }
            p.guardar();

            // limpiar carrito
            carritoDetalles.clear();
            modeloCarrito.setRowCount(0);
            actualizarTotalCarrito();
            txtPedidoClienteId.setText("");

            JOptionPane.showMessageDialog(this, "Pedido guardado. ID: " + p.getId());
            refrescarTablaCocina();
        } catch (Exception ex) {
            mostrarError("Guardar pedido", ex);
        }
    }

    // ===================== PANEL COCINA =====================
    private JPanel crearPanelCocina() {
        JPanel panel = new JPanel(new BorderLayout());

        modeloCocina = new DefaultTableModel(new Object[]{"ID Pedido", "Cliente", "Estado", "Total", "Fecha"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCocina = new JTable(modeloCocina);
        panel.add(new JScrollPane(tablaCocina), BorderLayout.CENTER);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnEnPrep = new JButton("Marcar EN_PREPARACION");
        JButton btnListo = new JButton("Marcar LISTO");
        JButton btnRefrescar = new JButton("Refrescar");
        acciones.add(btnEnPrep);
        acciones.add(btnListo);
        acciones.add(btnRefrescar);
        panel.add(acciones, BorderLayout.SOUTH);

        btnRefrescar.addActionListener(e -> refrescarTablaCocina());
        btnEnPrep.addActionListener(e -> actualizarEstadoSeleccion(EstadoPedido.EN_PREPARACION));
        btnListo.addActionListener(e -> {
            actualizarEstadoSeleccion(EstadoPedido.LISTO);
            // notificar si quedó listo
            int row = tablaCocina.getSelectedRow();
            if (row >= 0) {
                int idPedido = (int) modeloCocina.getValueAt(row, 0);
                // crear un Pedido minimal para notificar
                try {
                    Cliente cliente = new Cliente(0, "-", null, null); // dummy (no se usa)
                    Pedido p = new Pedido(cliente);
                    // hack: asignar id por reflexión no es buena práctica; mejor consultar al modelo real.
                    // Para simplicidad en esta demo, solo notificamos con el id.
                    // Extiende tu clase Pedido para un buscarPorId si quieres un objeto completo.
                    //
                    // Notificamos por consola
                    System.out.println("[NOTIFICACIÓN] Pedido #" + idPedido + " está LISTO para el mesero.");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        return panel;
    }

    private void refrescarTablaCocina() {
        modeloCocina.setRowCount(0);
        final String sql = "SELECT p.id, c.nombre AS cliente, p.estado, p.total, p.fecha_creacion " +
                "FROM pedido p JOIN cliente c ON c.id = p.id_cliente " +
                "ORDER BY p.id DESC";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloCocina.addRow(new Object[]{
                        rs.getInt(1), rs.getString(2), rs.getString(3), rs.getBigDecimal(4), rs.getTimestamp(5)
                });
            }
        } catch (SQLException e) {
            mostrarError("Cargar pedidos", e);
        }
    }

    private void actualizarEstadoSeleccion(EstadoPedido nuevo) {
        int row = tablaCocina.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un pedido");
            return;
        }
        int idPedido = (int) modeloCocina.getValueAt(row, 0);
        String estadoActual = String.valueOf(modeloCocina.getValueAt(row, 2));

        try {
            // Validar transición con el enum
            EstadoPedido actualEnum = EstadoPedido.valueOf(estadoActual);
            if (!actualEnum.puedeTransitarA(nuevo)) {
                JOptionPane.showMessageDialog(this, "Transición inválida: " + actualEnum + " → " + nuevo);
                return;
            }

            // Actualizar directamente por SQL (simple) o usa tu modelo Pedido si tienes buscarPorId
            final String sql = "UPDATE pedido SET estado=? WHERE id=?";
            try (Connection conn = ConexionDB.conectar();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuevo.name());
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevo);
            refrescarTablaCocina();
        } catch (Exception ex) {
            mostrarError("Actualizar estado", ex);
        }
    }

    // ===================== UTILS =====================
    private void mostrarError(String titulo, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, titulo + ":\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        // Si tu driver requiere registro explícito, descomenta:
        // try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new CafeteriaUI().setVisible(true));
    }
}

