
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL  = "jdbc:mysql://localhost:3306/cafeteria?useSSL=false&serverTimezone=UTC";
    private static final String USER = "";
    private static final String PASS = "";

    private ConexionDB() {}

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

