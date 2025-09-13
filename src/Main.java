// Main.java (ejemplo)
public class Main {
    public static void main(String[] args) {
        // 1) Crear/guardar cliente
        Cliente c = new Cliente("Juan Pérez", "juan@example.com", "0999999999");
        c.guardar();

        // 2) Conseguir un mesero (id_usuario) existente. Si sabes que es 1, úsalo directo.
        Integer idMesero = obtenerPrimerMeseroId(); // o Integer.valueOf(1);
        String numeroMesa = "A1"; // o null si no deseas mesa

        // 3) Crear pedido con el nuevo ctor
        Pedido p = new Pedido(c, idMesero, numeroMesa);

        // 4) Agregar items existentes (ids reales de tu tabla menu_item)
        MenuItem cafe = MenuItem.buscarPorId(1).orElseThrow();
        p.agregarDetalle(cafe, 2);

        // 5) Guardar
        p.guardar();
        System.out.println("Pedido creado: " + p.getId());
    }

    // helper simple (usa el primero que encuentre)
    private static Integer obtenerPrimerMeseroId() {
        final String sql = "SELECT id_usuario FROM usuario WHERE rol='MESERO' ORDER BY id_usuario LIMIT 1";
        try (java.sql.Connection conn = ConexionDB.conectar();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
            throw new RuntimeException("No hay usuarios con rol MESERO en la tabla 'usuario'.");
        } catch (java.sql.SQLException e) {
            throw new RuntimeException("Error al obtener id de mesero", e);
        }
    }
}

