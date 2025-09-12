
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL  = "jdbc:mysql://b83vne4akirattmdvghg-mysql.services.clever-cloud.com:3306/b83vne4akirattmdvghg?useSSL=false&serverTimezone=UTC";
    private static final String USER = "ursf1s0kkwntmweq";
    private static final String PASS = "3VcIZEQMJvUo9RpTTEaM";

    private ConexionDB() {}

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

