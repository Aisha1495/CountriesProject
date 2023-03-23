package CountriesProj;

import java.sql.*;

public class DatabaseInitializer {
    public void initializeDatabase(String Countries) {
        Connection conn = null;
        Statement stmt = null;

        DatabaseInitializer initializer = new DatabaseInitializer();
        initializer.initializeDatabase("Countries");

        
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            // Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + Countries, "root", "");

            // Execute a query to check if tables exist
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet tables = dbm.getTables(null, null, "country", null);

            // If tables do not exist, create them
            if (!tables.next()) {
                System.out.println("Creating tables in database...");

                // Create country table
                stmt = conn.createStatement();
                String sql = "CREATE TABLE country " +
                             "(id INT(11) NOT NULL AUTO_INCREMENT, " +
                             " name VARCHAR(255), " +
                             " capital VARCHAR(255), " +
                             " region VARCHAR(255), " +
                             " PRIMARY KEY (id))";
                stmt.executeUpdate(sql);
                System.out.println("Country table created successfully.");

                // Create city table
                stmt = conn.createStatement();
                sql = "CREATE TABLE city " +
                      "(id INT(11) NOT NULL AUTO_INCREMENT, " +
                      " name VARCHAR(255), " +
                      " country_id INT(11), " +
                      " PRIMARY KEY (id), " +
                      " FOREIGN KEY (country_id) REFERENCES country(id))";
                stmt.executeUpdate(sql);
                System.out.println("City table created successfully.");
            } else {
                System.out.println("Tables already exist in database.");
            }
        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
    }
}

