package CountriesProj;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.JSONParser;

public class CountriesMain {


	    private static final String DATABASE_NAME = "Countries";
	    private static final String DATABASE_USERNAME = "sa";
	    private static final String DATABASE_PASSWORD = "root";
	    private static final String TABLE_NAME = "countries";

	    public static void main(String[] args) {
	        try {
	            // Initialize database
	            initializeDatabase();

	            // Fetch data from API
	            JSONArray data = fetchApiData();

	            // Insert data into database
	            insertData(data);

	            // Fetch data from database and print it
	            fetchData();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    private static void initializeDatabase() throws ClassNotFoundException, SQLException {
	        // Load the SQL Server JDBC driver
	        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

	        // Connect to the database
	        Connection connection = DriverManager.getConnection(
	                "jdbc:sqlserver://localhost:1433;databaseName=" + DATABASE_NAME,
	                DATABASE_USERNAME,
	                DATABASE_PASSWORD);

	        // Create the table if it doesn't exist
	        Statement statement = connection.createStatement();
	        statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
	                + "id INT PRIMARY KEY IDENTITY,"
	                + "name VARCHAR(255),"
	                + "capital VARCHAR(255),"
	                + "region VARCHAR(255),"
	                + "population BIGINT,"
	                + "flag VARCHAR(255)"
	                + ")");

	        // Close the connection
	        connection.close();
	    }

	    private static JSONArray fetchApiData() throws Exception {
	        // Create a URL object
	        URL url = new URL("https://restcountries.com/v3.1/all");

	        // Create an HTTP connection
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	        // Set the request method
	        connection.setRequestMethod("GET");

	        // Read the response
	        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        StringBuilder response = new StringBuilder();
	        while ((line = reader.readLine()) != null) {
	            response.append(line);
	        }
	        reader.close();

	        // Parse the JSON data
	        JSONParser parser = new JSONParser();
	        JSONArray data = (JSONArray) parser.parse(response.toString());

	        return data;
	    }

	    private static void insertData(JSONArray data) throws ClassNotFoundException, SQLException {
	        // Load the SQL Server JDBC driver
	        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

	        // Connect to the database
	        Connection connection = DriverManager.getConnection(
	                "jdbc:sqlserver://localhost:1433;databaseName=" + DATABASE_NAME,
	                DATABASE_USERNAME,
	                DATABASE_PASSWORD);

	        // Insert the data into the table
	        String query = "INSERT INTO " + TABLE_NAME + " (name, capital, region, population, flag) VALUES (?, ?, ?, ?, ?)";
	        PreparedStatement statement = connection.prepareStatement(query);
	        for (Object obj : data) {
	            JSONObject country = (JSONObject) obj;
	            statement.setString(1, (String) country.get("name").toString());
	            statement.setString(2, (String) country.get("capital").toString());
	            statement.setString(3, (String) country.get("region").toString());
	            Long population = country.get("population") instanceof Long ? (Long) country.get("population") : Long.parseLong((String) country.get("population"));
	            statement.setLong(4, population);
	            statement.setString(5, (String) country.get("flag").toString());
	            statement.executeUpdate();
	        }



	        // Close the connection
	        connection.close();
	    }

	    private static void fetchData() throws ClassNotFoundException, SQLException {
	        // Load the SQL Server JDBC driver
	        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

	        // Connect to the database
	        Connection connection = DriverManager.getConnection(
	                "jdbc:sqlserver://localhost:1433;databaseName=" + DATABASE_NAME,
	                DATABASE_USERNAME,
	                DATABASE_PASSWORD);

	        // Fetch the data from the table
	        String query = "SELECT * FROM " + TABLE_NAME;
	        Statement statement = connection.createStatement();
	        ResultSet resultSet = statement.executeQuery(query);

	        // Print the data
	        while (resultSet.next()) {
	            System.out.println("id: " + resultSet.getInt("id"));
	            System.out.println("name: " + resultSet.getString("name"));
	            System.out.println("capital: " + resultSet.getString("capital"));
	            System.out.println("region: " + resultSet.getString("region"));
	            System.out.println("population: " + resultSet.getLong("population"));
	            System.out.println("flag: " + resultSet.getString("flag"));
	            System.out.println();
	        }

	        // Close the connection
	        connection.close();
	    }
}
