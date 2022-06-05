package scheduler.records;

import scheduler.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds records for First Level Divisions and functions related to them.
 * @author Ken Butler
 */
public record Divisions(int id, String name, Country country) {
    private static final HashMap<Integer, Divisions> divisions = new HashMap<>();

    /**
     * Returns a division by ID.
     * @param division_id The Division's ID.
     * @return The Division. Will return null if division is not found.
     */
    public static Divisions get(int division_id){
        if(division_id<0)
            return null;
        if(divisions.containsKey(division_id))
            return divisions.get(division_id);
        ResultSet rs = JDBC.getResults("SELECT Division, Country_ID from first_level_divisions WHERE Division_ID="+division_id+";");
        if(rs!=null){
            try {
                if (rs.next()) {
                    Divisions division = new Divisions(division_id, rs.getString(1),
                            Country.get(rs.getInt(2)));
                    divisions.put(division_id, division);
                    return division;
                }
            }catch(SQLException ignored){}
        }
        return null;
    }

    /**
     * Returns all divisions from the database.
     * @return The list of Divisions.
     */
    public static Collection<Divisions> getDivisions(){
        JDBC.process("SELECT Division_ID, Division, Country_ID from first_level_divisions", (rs, row, count)->{
            if(!divisions.containsKey(rs.getInt(1))){
                divisions.put(rs.getInt(1),new Divisions(rs.getInt(1),rs.getString(2),Country.get(rs.getInt(3))));
            }
        });
        return divisions.values();
    }

    /**
     * <p>Returns divisions belonging to a specific {@link Country}.</p>
     * <p>Lambda is used to filter the divisions and compare them to the country.</p>
     * @param country The {@link Country} to filter Divisions by.
     * @return The list of Divisions belonging to the {@link Country}.
     */
    public static Collection<Divisions> getDivisions(Country country){
        return getDivisions().stream().filter(d->d.country==country).toList();
    }

    @Override
    public String toString() {
        return name;
    }
}
