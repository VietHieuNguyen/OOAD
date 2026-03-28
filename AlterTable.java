import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class AlterTable {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/bookstore?useSSL=false&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8", "root", "");
            Statement stmt = conn.createStatement();
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN token VARCHAR(500)");
                System.out.println("Added token column.");
            } catch (Exception e) {
                System.out.println("Token column may already exist: " + e.getMessage());
            }
            try {
                stmt.execute("ALTER TABLE users ADD COLUMN avatar VARCHAR(500)");
                System.out.println("Added avatar column.");
            } catch (Exception e) {
                System.out.println("Avatar column may already exist: " + e.getMessage());
            }
            conn.close();
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
