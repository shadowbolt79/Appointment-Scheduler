package scheduler.records;

import scheduler.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds records for Countries and functions related to them.
 * @author Ken Butler
 */
public record Country(int id, String name) {
    private static final HashMap<Integer, Country> countries = new HashMap<>();

    /**
     * Returns a country by its ID. Returns null if not found.
     * @param id The Country's ID.
     * @return The Country. Returns null if country is not found.
     */
    public static Country get(int id){
        if(countries.containsKey(id))
            return countries.get(id);
        ResultSet rs = JDBC.getResults("SELECT Country from countries WHERE Country_ID="+id+";");
        if(rs!=null){
            try {
                if (rs.next()) {
                    Country country = new Country(id, rs.getString(1));
                    countries.put(id, country);
                    return country;
                }
            }catch(SQLException ignored){}
        }
        return null;
    }

    /**
     * Returns a list of all countries, collected from the database.
     * @return The list of all countries.
     */
    public static Collection<Country> getCountries() {
        JDBC.process("SELECT Country_ID, Country from countries", (rs, row, count)->{
            if(!countries.containsKey(rs.getInt(1))){
                countries.put(rs.getInt(1),new Country(rs.getInt(1),rs.getString(2)));
            }
        });
        return countries.values();
    }

    @Override
    public String toString() {
        return name;
    }
}
