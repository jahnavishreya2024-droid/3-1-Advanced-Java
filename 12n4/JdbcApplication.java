import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcApplication {

    // Database connection details
    private static final String URL = "jdbc:mariadb://localhost:3306/enterprisedb";
    private static final String USER = "java_dev";
    private static final String PASSWORD = "SecurePass123";

    public static void main(String[] args) {
        System.out.println("=== Initiating Enterprise JDBC Application ===");

        try {
            // Step 1: Initialize Database Infrastructure
            createTable();

            // Step 2: Perform CRUD Operations
            insertRecord(101, "Alice Vance", "Engineering", 95000.00);
            insertRecord(102, "Bob Miller", "Product", 88000.00);
            
            System.out.println("\n--- Initial State ---");
            displayRecords();

            // Step 3: Update 
            updateRecordSalary(101, 102000.00);
            
            System.out.println("\n--- After Salary Update ---");
            displayRecords();

            // Step 4: Delete
            deleteRecord(102);

            System.out.println("\n--- Final Operational State ---");
            displayRecords();

        } catch (SQLException e) {
            System.err.println("Fatal Database Error Engine: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reusable database connection provider
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * DDL Operation: Create Table
     */
    private static void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS employees ("
                   + "id INT PRIMARY KEY, "
                   + "name VARCHAR(100) NOT NULL, "
                   + "department VARCHAR(50), "
                   + "salary DECIMAL(10, 2)"
                   + ")";
                   
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("[SUCCESS] Database structural synchronization complete: 'employees' table is ready.");
        }
    }

    /**
     * DML Operation: Insert Record (Secure via PreparedStatement)
     */
    private static void insertRecord(int id, String name, String dept, double salary) throws SQLException {
        String sql = "INSERT INTO employees (id, name, department, salary) VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE name=?, department=?, salary=?";
                   
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            // Setting values for insert
            pstmt.setInt(1, id);
            pstmt.setString(2, name);
            pstmt.setString(3, dept);
            pstmt.setDouble(4, salary);
            
            // Setting values for fallback update on duplicate key (so script re-runs safely)
            pstmt.setString(5, name);
            pstmt.setString(6, dept);
            pstmt.setDouble(7, salary);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("[INSERT/UPDATE] Synchronized Employee ID: " + id);
            }
        }
    }

    /**
     * DML Operation: Update Record
     */
    private static void updateRecordSalary(int id, double newSalary) throws SQLException {
        String sql = "UPDATE employees SET salary = ? WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newSalary);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[UPDATE] Modified " + rowsAffected + " record(s). Employee " + id + " updated.");
        }
    }

    /**
     * DML Operation: Delete Record
     */
    private static void deleteRecord(int id) throws SQLException {
        String sql = "DELETE FROM employees WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("[DELETE] Processed removal for Employee ID: " + id + ". Target rows: " + rowsAffected);
        }
    }

    /**
     * DQL Operation: Fetch and Display Data
     */
    private static void displayRecords() throws SQLException {
        String sql = "SELECT id, name, department, salary FROM employees";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
             
            System.out.printf("%-6s | %-15s | %-12s | %-10s%n", "ID", "NAME", "DEPT", "SALARY");
            System.out.println("---------------------------------------------------------");
            
            boolean recordsFound = false;
            while (rs.next()) {
                recordsFound = true;
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String dept = rs.getString("department");
                double salary = rs.getDouble("salary");
                
                System.out.printf("%-6d | %-15s | %-12s | $%-9.2f%n", id, name, dept, salary);
            }
            if (!recordsFound) {
                System.out.println("[INFO] No records currently exist inside 'employees' table.");
            }
        }
    }
}
