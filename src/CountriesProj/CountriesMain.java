	package CountriesProj;

	import java.io.*;
	import java.net.*;
	import java.nio.charset.StandardCharsets;
	import java.sql.*;
	import java.util.Scanner;

	import org.json.*;

public class CountriesMain {



	    private static final String API_URL = "https://restcountries.com/v3.1/all";

	    private static final String CREATE_COUNTRY_TABLE_SQL =
	        "CREATE TABLE IF NOT EXISTS country (" +
	            "id INT(11) NOT NULL AUTO_INCREMENT, " +
	            "name VARCHAR(255), " +
	            "capital VARCHAR(255), " +
	            "region VARCHAR(255), " +
	            "PRIMARY KEY (id)" +
	        ")";

	    private static final String CREATE_CITY_TABLE_SQL =
	        "CREATE TABLE IF NOT EXISTS city (" +
	            "id INT(11) NOT NULL AUTO_INCREMENT, " +
	            "name VARCHAR(255), " +
	            "country_id INT(11), " +
	            "PRIMARY KEY (id), " +
	            "FOREIGN KEY (country_id) REFERENCES country(id)" +
	        ")";

	    private static final String INSERT_COUNTRY_SQL =
	        "INSERT INTO country (name, capital, region) VALUES (?, ?, ?)";

	    private static final String INSERT_CITY_SQL =
	        "INSERT INTO city (name, country_id) VALUES (?, ?)";

	    private static final String SELECT_COUNTRY_BY_CAPITAL_SQL =
	        "SELECT * FROM country WHERE capital = ?";

	    private static final String SELECT_COUNTRY_BY_REGION_SQL =
	        "SELECT * FROM country WHERE region = ?";

	    private static final String SELECT_CITY_BY_COUNTRY_SQL =
	        "SELECT * FROM city WHERE country_id = ?";

	    private static final String BACKUP_FILE_PATH = "countries_backup.sql";

	    private static final Scanner scanner = new Scanner(System.in);

	    private static Connection connection;

	    public static void main(String[] args) {
	        initializeDatabase();
	        boolean exit = false;
	        while (!exit) {
	            System.out.println("Enter an option:\n" +
	                "1. Fetch data from API\n" +
	                "2. Fetch data from database\n" +
	                "3. Search countries by capital or region\n" +
	                "4. Backup database\n" +
	                "5. Remove tables from database\n" +
	                "6. Exit");
	            int option = scanner.nextInt();
	            scanner.nextLine(); // consume the newline character

	            switch (option) {
	                case 1:
	                    fetchFromApi();
	                    break;
	                case 2:
	                    fetchFromDatabase();
	                    break;
	                case 3:
	                    searchCountries();
	                    break;
	                case 4:
	                    backupDatabase();
	                    break;
	                case 5:
	                    removeTables();
	                    break;
	                case 6:
	                    exit = true;
	                    break;
	                default:
	                    System.out.println("Invalid option, try again");
	                    break;
	            }
	        }
	    }

	    private static void initializeDatabase() {
	        try {
	            // Get database credentials from user
	            System.out.print("Enter MySQL username: ");
	            String username = scanner.nextLine();
	            System.out.print("Enter MySQL password: ");
	            String password = scanner.nextLine();

	            // Open a connection
	            System.out.println("Connecting to database...");
	            connection = DriverManager.getConnection("jdbc:mysql://localhost", username, password);

	            // Create the database if it does not exist
	            Statement statement = connection.createStatement();
	            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS CountriesProj");
	            statement.close();

	            // Select the database
	            connection.setCatalog("CountriesProj");

	            // Create tables if they do not exist
	            statement = connection.createStatement();
	            statement.executeUpdate(CREATE_CITY_TABLE_SQL);
	            statement.close();
	            System.out.println("Database initialized successfully");
	            } catch (SQLException e) {
	            System.out.println("Failed to initialize database: " + e.getMessage());
	            }
	            }
	    
	    private static void fetchFromApi() {
	        try {
	            System.out.println("Fetching data from API...");
	            URL url = new URL(API_URL);
	            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	            conn.setRequestMethod("GET");

	            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
	            StringBuilder responseBuilder = new StringBuilder();
	            String responseLine;
	            while ((responseLine = in.readLine()) != null) {
	                responseBuilder.append(responseLine);
	            }
	            in.close();

	            String response = responseBuilder.toString();
	            JSONArray countriesArray = new JSONArray(response);

	            PreparedStatement countryStatement = connection.prepareStatement(INSERT_COUNTRY_SQL);
	            PreparedStatement cityStatement = connection.prepareStatement(INSERT_CITY_SQL);

	            for (int i = 0; i < countriesArray.length(); i++) {
	            	JSONObject countryObject = countriesArray.getJSONObject(i).getJSONObject("name");

	            	String countryName = countryObject.getString("common");
	            	String capital = null;
	            	if (countryObject.has("capital")) {
	            	    capital = countryObject.getJSONObject("capital").getString(0);
	            	}
	            	String region = null;
	            	if (countryObject.has("region")) {
	            	    region = countryObject.getString("region");
	            	}

	            	countryStatement.setString(1, countryName);
	            	countryStatement.setString(2, capital);
	            	countryStatement.setString(3, region);
	            	countryStatement.executeUpdate();


	                ResultSet rs = countryStatement.getGeneratedKeys();
	                int countryId = -1;
	                if (rs.next()) {
	                    countryId = rs.getInt(1);
	                }

	                JSONArray citiesArray = countriesArray.getJSONObject(i).getJSONArray("city");
	                for (int j = 0; j < citiesArray.length(); j++) {
	                    String cityName = citiesArray.getString(j);
	                    cityStatement.setString(1, cityName);
	                    cityStatement.setInt(2, countryId);
	                    cityStatement.executeUpdate();
	                }
	            }

	            countryStatement.close();
	            cityStatement.close();

	            System.out.println("Data fetched from API successfully");
	        } catch (IOException | SQLException | JSONException e) {
	            System.out.println("Failed to fetch data from API: " + e.getMessage());
	        }
	    }

	    private static void fetchFromDatabase() {
	        try {
	            Statement statement = connection.createStatement();
	            ResultSet resultSet = statement.executeQuery("SELECT * FROM country");
	            while (resultSet.next()) {
	                System.out.printf("%-20s %-20s %-20s\n",
	                        resultSet.getString("name"),
	                        resultSet.getString("capital"),
	                        resultSet.getString("region"));

	                int countryId = resultSet.getInt("id");
	                PreparedStatement cityStatement = connection.prepareStatement(SELECT_CITY_BY_COUNTRY_SQL);
	                cityStatement.setInt(1, countryId);
	                ResultSet cityResultSet = cityStatement.executeQuery();
	                while (cityResultSet.next()) {
	                    System.out.printf("%-40s %s\n", "", cityResultSet.getString("name"));
	                }
	                cityResultSet.close();
	                cityStatement.close();
	            }
	            resultSet.close();
	            statement.close();
	        } catch (SQLException e) {
	            System.out.println("Failed to fetch data from database: " + e.getMessage());
	        }
	    }

	    private static void removeTables() {
	        try {
	            Statement statement = connection.createStatement();
	            statement.executeUpdate("DROP TABLE IF EXISTS city");
	            statement.executeUpdate("DROP TABLE IF EXISTS country");
	            statement.close();
	            System.out.println("Tables removed successfully");
	        } catch (SQLException e) {
	            System.out.println("Failed to remove tables: " + e.getMessage());
	        }
	    }

	    private static void backupDatabase() {
	        try {
	            // Create backup file
	            FileWriter writer = new FileWriter("C:\\Users\\Lenovo\\eclipse-workspace\\CountriesProject\\countries_backup.sql");
	            System.out.println("Creating backup file...");
	            // Initialize process for running mysqldump command
	            String[] cmd = new String[]{"mysqldump", "--user=root", "--password=root", "CountriesProj"};
	            Process process = Runtime.getRuntime().exec(cmd);
	            // Pipe output of mysqldump to backup file
	            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	            String line;
	            while ((line = reader.readLine()) != null) {
	                writer.write(line + "\n");
	            }
	            writer.close();
	            reader.close();
	            System.out.println("Database backed up successfully to " + "C:\\Users\\Lenovo\\eclipse-workspace\\CountriesProject\\countries_backup.sql");

	        } catch (IOException e) {
	            System.out.println("Failed to backup database: " + e.getMessage());
	        }
	    }
	    
	    private static void searchCountries() {
	        System.out.println("Enter an option:\n" +
	            "1. Search countries by capital\n" +
	            "2. Search countries by region");
	        int option = scanner.nextInt();
	        scanner.nextLine(); // consume the newline character

	        try {
	            PreparedStatement statement;
	            switch (option) {
	                case 1:
	                    System.out.print("Enter capital name: ");
	                    String capital = scanner.nextLine();
	                    statement = connection.prepareStatement(SELECT_COUNTRY_BY_CAPITAL_SQL);
	                    statement.setString(1, capital);
	                    break;
	                case 2:
	                    System.out.print("Enter region name: ");
	                    String region = scanner.nextLine();
	                    statement = connection.prepareStatement(SELECT_COUNTRY_BY_REGION_SQL);
	                    statement.setString(1, region);
	                    break;
	                default:
	                    System.out.println("Invalid option, try again");
	                    return;
	            }

	            ResultSet resultSet = statement.executeQuery();

	            while (resultSet.next()) {
	                System.out.println(resultSet.getString("name") + " (" +
	                        resultSet.getString("capital") + ") - " +
	                        resultSet.getString("region"));
	            }

	            resultSet.close();
	            statement.close();

	        } catch (SQLException e) {
	            System.out.println("Failed to search countries: " + e.getMessage());
	        }
	    }
}