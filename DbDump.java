import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbDump {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8", "root", "123456");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT user_id, username, password_hash, token FROM users");
            while(rs.next()) {
                System.out.println("User: " + rs.getString("username"));
                System.out.println("  Hash: " + rs.getString("password_hash"));
                System.out.println("  Token: " + (rs.getString("token") != null ? "exists" : "null"));
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
