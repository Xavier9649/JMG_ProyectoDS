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
 * UI Swing para el proyecto de cafetería (actualizado):
 * - En "Pedidos (Mesero)" ahora puedes seleccionar productos del menú desde un JComboBox
 *   (en lugar de escribir el ID). Se cargan desde la tabla menu_item (solo activos).
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
    private JTextField txtMeseroId;     // opcional
    private JTextField txtNumeroMesa;   // opcional
    private JTextField txtCantidad;
    private JTable tablaCarrito;
    private DefaultTableModel modeloCarrito;
    private JLabel lblTotalPedido;

    private final List<DetallePedido> carritoDetalles = new ArrayList<>();

    // Menú (nuevo)
    private JComboBox<MenuItem> comboMenu;                 // muestra nombre + precio
    private final List<MenuItem> opcionesMenu = new ArrayList<>(); // cache en memoria

    // ===== COCINA =====
    private JTable tablaCocina;
    private DefaultTableModel modeloCocina;

    private final NotificacionService notificador = new NotificacionService();

    public CafeteriaUI() {
        super("Cafetería - Gestión (Swing)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1040, 740);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Clientes", crearPanelClientes());
        tabs.addTab("Pedidos (Mesero)", crearPanelPedidos());
        tabs.addTab("Cocina", crearPanelCocina());
        add(tabs, BorderLayout.CENTER);

        // Cargar listados iniciales
        refrescarTablaClientes();
        cargarMenuDesdeBD();
        refrescarTablaCocina();
    }

    // ===================== PANEL CLIENTES =====================
    private JPanel crearPanelClientes() {
        JPanel panel = new JPanel(new BorderLayout());

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

        modeloClientes = new DefaultTableModel(new Object[]{"ID", "Nombre", "Email", "Teléfono"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaClientes = new JTable(modeloClientes);
        panel.add(new JScrollPane(tablaClientes), BorderLayout.CENTER);

        btnGuardar.addActionListener(this::accionGuardarCliente);
        btnBuscar.addActionListener(this::accionBuscarCliente);
        btnActualizar.addActionListener(this::accionActualizarCliente);
        btnEliminar.addActionListener(this::accionEliminarCliente);
        btnRefrescar.addActionListener(e -> refrescarTablaClientes());

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
        final String sql = "SELECT id_cliente AS id, nombre, correo AS email, telefono FROM cliente ORDER BY id_cliente DESC";
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

        // --- Barra superior (datos y agregar al carrito) ---
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtPedidoClienteId = new JTextField(6);
        txtMeseroId = new JTextField(6);
        txtNumeroMesa = new JTextField(6);
        comboMenu = new JComboBox<>();
        txtCantidad = new JTextField(4);

        JButton btnAddItem = new JButton("Agregar al carrito");
        JButton btnRefrescarMenu = new JButton("Actualizar menú");
        lblTotalPedido = new JLabel("Total: $0.00");

        top.add(new JLabel("ID Cliente:"));
        top.add(txtPedidoClienteId);
        top.add(Box.createHorizontalStrut(8));
        top.add(new JLabel("ID Mesero:"));
        top.add(txtMeseroId);
        top.add(Box.createHorizontalStrut(8));
        top.add(new JLabel("N° Mesa:"));
        top.add(txtNumeroMesa);

        top.add(Box.createHorizontalStrut(12));
        top.add(new JLabel("Producto:"));
        top.add(comboMenu);
        top.add(new JLabel("Cant:"));
        top.add(txtCantidad);
        top.add(btnAddItem);
        top.add(Box.createHorizontalStrut(8));
        top.add(btnRefrescarMenu);

        panel.add(top, BorderLayout.NORTH);

        // --- Tabla del carrito ---
        modeloCarrito = new DefaultTableModel(
                new Object[]{"ID Item", "Nombre", "Precio", "Cantidad", "Subtotal"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCarrito = new JTable(modeloCarrito);
        panel.add(new JScrollPane(tablaCarrito), BorderLayout.CENTER);

        // --- Barra inferior fija con Guardar Pedido y Total (SIEMPRE visible) ---
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardarPedido = new JButton("Guardar Pedido");
        bottom.add(lblTotalPedido);
        bottom.add(btnGuardarPedido);
        panel.add(bottom, BorderLayout.SOUTH);

        // Listeners
        btnAddItem.addActionListener(this::accionAgregarItemCarritoDesdeCombo);
        btnRefrescarMenu.addActionListener(e -> cargarMenuDesdeBD());
        btnGuardarPedido.addActionListener(this::accionGuardarPedido);

        return panel;
    }


    // Carga el menú desde BD a la lista + combo
    private void cargarMenuDesdeBD() {
        opcionesMenu.clear();
        comboMenu.removeAllItems();
        final String sql = "SELECT id_item AS id, nombre, precio FROM menu_item ORDER BY nombre";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MenuItem mi = new MenuItem(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getBigDecimal("precio"),
                        null
                );
                opcionesMenu.add(mi);
                comboMenu.addItem(mi);
            }
        } catch (SQLException e) { mostrarError("Cargar menú", e); }
    }



    private void accionAgregarItemCarritoDesdeCombo(ActionEvent e) {
        try {
            Object sel = comboMenu.getSelectedItem();
            if (!(sel instanceof MenuItem mi)) {
                JOptionPane.showMessageDialog(this, "Selecciona un producto del menú");
                return;
            }
            int cant = Integer.parseInt(txtCantidad.getText().trim());
            if (cant <= 0) throw new IllegalArgumentException("Cantidad debe ser > 0");

            DetallePedido det = new DetallePedido(mi, cant);
            carritoDetalles.add(det);

            BigDecimal subtotal = det.getSubtotal();
            modeloCarrito.addRow(new Object[]{mi.getId(), mi.getNombre(), mi.getPrecio(), cant, subtotal});

            actualizarTotalCarrito();
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

    private Integer parseNullableInt(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        return Integer.parseInt(s);
    }

    private void accionGuardarPedido(ActionEvent e) {
        try {
            int idCliente = Integer.parseInt(txtPedidoClienteId.getText().trim());
            Integer idMesero = Integer.parseInt(txtMeseroId.getText().trim());
            String numeroMesa = txtNumeroMesa.getText().trim();
            if (carritoDetalles.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Carrito vacío");
                return;
            }
            var oc = Cliente.buscarPorId(idCliente);
            if (oc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cliente no existe");
                return;
            }
            Pedido p = new Pedido(oc.get(), idMesero, numeroMesa.isEmpty()? null : numeroMesa);
            for (DetallePedido d : carritoDetalles) p.agregarDetalle(d.getItem(), d.getCantidad());
            p.guardar();

            carritoDetalles.clear();
            modeloCarrito.setRowCount(0);
            actualizarTotalCarrito();
            JOptionPane.showMessageDialog(this, "Pedido guardado. ID: " + p.getId());
            refrescarTablaCocina(); // para verlo en la pestaña Cocina
        } catch (Exception ex) {
            mostrarError("Guardar pedido", ex);
        }
    }



    // ===================== PANEL COCINA =====================
    private JPanel crearPanelCocina() {
        JPanel panel = new JPanel(new BorderLayout());

        modeloCocina = new DefaultTableModel(
                new Object[]{"ID Pedido", "Cliente", "ID Mesero", "N° Mesa", "Estado", "Total", "Fecha"}, 0) {
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
            int row = tablaCocina.getSelectedRow();
            if (row >= 0) {
                Object val = modeloCocina.getValueAt(row, 0);
                int idPedido = (val instanceof Number) ? ((Number) val).intValue() : Integer.parseInt(val.toString());
                System.out.println("[NOTIFICACIÓN] Pedido #" + idPedido + " está LISTO para el mesero.");
            }
        });

        return panel;
    }

    private void refrescarTablaCocina() {
        modeloCocina.setRowCount(0);
        final String sql =
                "SELECT p.id_pedido AS id, c.nombre AS cliente, p.id_mesero, p.numero_mesa, p.estado, p.total, p.fecha_creacion " +
                        "FROM pedido p JOIN cliente c ON c.id_cliente = p.id_cliente " +
                        "ORDER BY p.id_pedido DESC";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloCocina.addRow(new Object[]{
                        rs.getInt("id"), rs.getString("cliente"),
                        rs.getInt("id_mesero"), rs.getString("numero_mesa"),
                        rs.getString("estado"), rs.getBigDecimal("total"), rs.getTimestamp("fecha_creacion")
                });
            }
        } catch (SQLException e) { mostrarError("Cargar pedidos", e); }
    }


    private void actualizarEstadoSeleccion(EstadoPedido nuevo) {
        int row = tablaCocina.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Selecciona un pedido"); return; }
        int idPedido = Integer.parseInt(String.valueOf(modeloCocina.getValueAt(row, 0)));
        String estadoActualDb = String.valueOf(modeloCocina.getValueAt(row, 4));
        EstadoPedido actual = EstadoPedido.fromDb(estadoActualDb);

        try {
            if (!actual.puedeTransitarA(nuevo)) {
                JOptionPane.showMessageDialog(this, "Transición inválida: " + actual + " → " + nuevo); return;
            }
            final String sql = "UPDATE pedido SET estado=? WHERE id_pedido=?";
            try (Connection conn = ConexionDB.conectar(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nuevo.toDb());
                ps.setInt(2, idPedido);
                ps.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevo);
            refrescarTablaCocina();
        } catch (Exception ex) { mostrarError("Actualizar estado", ex); }
    }


    // ===================== UTILS =====================
    private void mostrarError(String titulo, Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,                           // parentComponent
                titulo + ":\n" + ex.getMessage(),// message
                "Error",                        // title
                JOptionPane.ERROR_MESSAGE       // messageType
        );
    }


    public static void main(String[] args) {
        // Si tu driver requiere registro explícito:
        // try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { e.printStackTrace(); }
        SwingUtilities.invokeLater(() -> new CafeteriaUI().setVisible(true));
    }
}



