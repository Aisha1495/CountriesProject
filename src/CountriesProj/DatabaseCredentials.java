package CountriesProj;

public class DatabaseCredentials {
    private String username;
    private String password;

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
