import java.sql.Connection;
import java.sql.SQLException;

public class TestConexion {
    public static void main(String[] args) {
        try (Connection conn = ConexionDB.conectar()) {
            if (conn != null) {
                System.out.println("✅ ¡Conexión exitosa a Clever Cloud!");
            } else {
                System.out.println("❌ No se pudo conectar.");
            }
        } catch (SQLException e) {
            System.out.println("⚠ Error al conectar: " + e.getMessage());
            e.printStackTrace();
        }
    }
}