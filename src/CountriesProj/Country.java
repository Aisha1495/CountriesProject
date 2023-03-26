package CountriesProj;

public class Country {

    private int id;
    private String name;
    private String capital;
    private String region;
    


    public Country(int id, String name, String capital, String region) {
        this.id = id;
        this.name = name;
        this.capital = capital;
        this.region = region;
    }

    public Country(String nString, String string, int int1) {
		// TODO Auto-generated constructor stub
	}

	public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "Country [id=" + id + ", name=" + name + ", capital=" + capital + ", region=" + region + "]";
    }

	public int getPopulation() {
		// TODO Auto-generated method stub
		return 0;
	}
}
