package CountriesProj;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;



public class CountriesMain {

    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=Countries;encrypt=true;trustServerCertificate=true";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWORD = "root";
    private static final String DB_NAME = "Countries";

    private static Connection conn;

    public static void main1(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            createDatabaseIfNotExists();
            createTablesIfNotExists();

            while (true) {
                System.out.println("1. Fetch data from API");
                System.out.println("2. Fetch data from database");
                System.out.println("3. Search data from API");
                System.out.println("4. Search data from database");
                System.out.println("5. Dump data to file");
                System.out.println("6. Load data from file");
                System.out.println("7. Backup database");
                System.out.println("8. Remove tables");
                System.out.println("9. Exit");

                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        fetchDataFromAPI();
                        break;
                    case 2:
                        fetchDataFromDatabase();
                        break;
                    case 3:
                        searchDataFromAPI();
                        break;
                    case 4:
                        searchDataFromDatabase();
                        break;
                    case 5:
                        dumpDataToFile();
                        break;
                    case 6:
                        loadDataFromFile();
                        break;
                    case 7:
                        backupDatabase();
                        break;
                    case 8:
                        removeTables();
                        break;
                    case 9:
                        System.out.println("Exiting...");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice!");
                        break;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createDatabaseIfNotExists() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
        stmt.close();
        conn.setCatalog(DB_NAME);
    }

    private static void createTablesIfNotExists() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS countries (" +
                "id INT PRIMARY KEY AUTO_INCREMENT," +
                "name VARCHAR(255) NOT NULL," +
                "capital VARCHAR(255) NOT NULL," +
                "region VARCHAR(255) NOT NULL" +
                ")");
        stmt.close();
    }
    
    private static void addCountryToDatabase(Country country) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO countries (name, capital, region) VALUES (?, ?, ?)");
        stmt.setString(1, country.getName());
        stmt.setString(2, country.getCapital());
        stmt.setString(3, country.getRegion());
        stmt.executeUpdate();
        stmt.close();
    }
    private static void fetchDataFromAPI() throws SQLException {
    	
    	
        List<Country> countries = getCountriesFromAPI();

        if (countries.isEmpty()) {
            System.out.println("No data found from API!");
            return;
        }

        for (Country country : countries) {
            addCountryToDatabase(country);
        }

        System.out.println("Data fetched and added to database.");
    }

    private static void fetchDataFromDatabase() throws SQLException {
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM countries");

        List<Country> countries= new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String capital = rs.getString("capital");
            String region = rs.getString("region");

            Country country = new Country(id, name, capital, region);
            countries.add(country);
        }

        rs.close();
        stmt.close();

        if (countries.isEmpty()) {
            System.out.println("No data found from database!");
            return;
        }

        for (Country country : countries) {
            System.out.println(country);
        }
    }

    private static void searchDataFromAPI() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the country name: ");
        String name = scanner.nextLine();

        List<Country> countries = getCountriesFromAPIByName(name);

        if (countries.isEmpty()) {
            System.out.println("No data found from API!");
            return;
        }

        for (Country country : countries) {
            System.out.println(country);
        }
    }

    private static List<Country> getCountriesFromAPIByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void searchDataFromDatabase() throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the country name: ");
        String name = scanner.nextLine();

        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM countries WHERE name LIKE ?");
        stmt.setString(1, "%" + name + "%");
        ResultSet rs = stmt.executeQuery();

        List<Country> countries = new ArrayList<>();

        while (rs.next()) {
            int id = rs.getInt("id");
            String countryName = rs.getString("name");
            String capital = rs.getString("capital");
            String region = rs.getString("region");

            Country country = new Country(id, countryName, capital, region);
            countries.add(country);
        }

        rs.close();
        stmt.close();

        if (countries.isEmpty()) {
            System.out.println("No data found from database!");
            return;
        }

        for (Country country : countries) {
            System.out.println(country);
        }
    }

    private static void dumpDataToFile() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the filename to dump data to: ");
        String filename = scanner.nextLine();

        PrintWriter writer = new PrintWriter(new FileWriter(filename));

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM countries");

        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String capital = rs.getString("capital");
            String region = rs.getString("region");

            writer.println(id + "," + name + "," + capital + "," + region);
        }

        rs.close();
        stmt.close();
        writer.close();

        System.out.println("Data dumped to file.");
    }

    private static void loadDataFromFile() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the filename to load data from: ");
        String filename = scanner.nextLine();

        BufferedReader reader = new BufferedReader(new FileReader(new File(filename)));

        String line;
        List<Country> countries = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");

            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            String capital = parts[2];
            String region = parts[3];

            Country country = new Country(id, name, capital, region);
            countries.add(country);
        }

        reader.close();

        if (countries.isEmpty()) {
            System.out.println("No data found from file!");
            return;
        }

        for (Country country : countries) {
            addCountryToDatabase(country);
        }

        System.out.println("Data loaded from file and added to database.");
    }

    private static void backupDatabase() throws SQLException, IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the filename to backup database to: ");
        String filename = scanner.nextLine();

        PrintWriter writer = new PrintWriter(new FileWriter(filename));

        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW TABLES");

        while (rs.next()) {
            String tableName = rs.getString(1);

            writer.println("DROP TABLE IF EXISTS " + tableName + ";");

            ResultSet rsTable = stmt.executeQuery("SHOW CREATE TABLE " + tableName);
            if (rsTable.next()) {
                String createTableSQL = rsTable.getString(2);
                writer.println(createTableSQL + ";");
            }
            rsTable.close();

            ResultSet rsData = stmt.executeQuery("SELECT * FROM " + tableName);
            while (rsData.next()) {
                StringBuilder data = new StringBuilder();
                data.append("INSERT INTO ").append(tableName).append(" VALUES (");
                for (int i = 1; i <= rsData.getMetaData().getColumnCount(); i++) {
                    if (i > 1) {
                        data.append(",");
                    }
                    Object value = rsData.getObject(i);
                    if (value == null) {
                        data.append("NULL");
                    } else if (value instanceof Number) {
                        data.append(value);
                    } else {
                        data.append("'").append(value).append("'");
                    }
                }
                data.append(");");
                writer.println(data);
            }
            rsData.close();

            writer.println();
        }

        rs.close();
        stmt.close();
        writer.close();

        System.out.println("Database backup created.");
    }

    private static void removeTables() throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate("DROP TABLE IF EXISTS countries");
        stmt.close();
        System.out.println("Tables removed.");
    }

    private static List<Country> getCountriesFromAPI() {
        List<Country> countries =new ArrayList<>();
     // code to retrieve data from API and populate the countries list
     // ...
     return countries;
     }

    private static void insertCountries(List<Country> countries) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("INSERT INTO countries (name, capital, population) VALUES (?, ?, ?)");
        for (Country country : countries) {
            pstmt.setString(1, country.getName());
            pstmt.setString(2, country.getCapital());
            pstmt.setInt(3, country.getPopulation());
            pstmt.executeUpdate();
        }
        pstmt.close();
        System.out.println("Countries inserted.");
    }

    private static List<Country> getCountriesFromDB() throws SQLException {
        List<Country> countries = new ArrayList<>();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM countries");
        while (rs.next()) {
            Country country = new Country(rs.getNString("name"), rs.getString("capital"), rs.getInt("population"));
            countries.add(country);
        }
        rs.close();
        stmt.close();
        return countries;
    }

    private static void displayCountries(List<Country> countries) {
        for (Country country : countries) {
            System.out.println(country.getName() + " (" + country.getCapital() + ") - Population: " + country.getPopulation());
        }
    }
//
//    public static void main(String[] args) {
//        try {
//            connect();
//            removeTables();
//            List<Country> countries = getCountriesFromAPI();
//            insertCountries(countries);
//            countries = getCountriesFromDB();
//            displayCountries(countries);
//            disconnect();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

}






