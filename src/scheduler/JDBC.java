package scheduler;
import java.sql.*;

/**
 * Handles connection to the database.
 */
public class JDBC {
    private static final String protocol = "jdbc";
    private static final String vendor = ":mysql:";
    private static final String location = "//localhost/";
    private static final String databaseName = "client_schedule";
    private static final String jdbcUrl = protocol + vendor + location + databaseName + "?connectionTimeZone = SERVER"; // LOCAL
    private static final String userName = "sqlUser"; // Username
    private static final String password = "Passw0rd!"; // Password
    private static Connection connection = null;  // Connection Interface

    /**
     * Functional interface to provide a function for lambda insertion into processing each row of a result set.
     */
    @FunctionalInterface
    public interface ResultSetProcessor {
        /**
         * <p>Function to process results from the query, called per row.</p>
         * <p>Do not call {@link ResultSet#next()} as it is called automatically.</p>
         * @param resultSet The set to pull information from.
         * @param row The row number it is currently on.
         * @param count The total number of rows.
         * @throws SQLException Thrown if there was a problem with getting information from the {@link ResultSet}.
         */
        void process(ResultSet resultSet, int row, int count) throws SQLException;
    }

    /**
     * <p>Connects to the database and stores the connection for later use.</p>
     * <p>Must be called before SQL Statement/Query functions will work.</p>
     */
    public static void makeConnection() {
        try {
            connection = DriverManager.getConnection(jdbcUrl, userName, password); // reference Connection object
            System.out.println("Connection successful!");
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
    }

    /**
     * Returns a ResultSet from the sql statement provided for the calling function to process.
     * @param sqlStatement The SQL statement/query
     * @param vars Optional additional vars. Will replace '?' within the query. Query will likely fail if
     *             the wrong number of vars are provided. Will only accept integers and strings.
     * @return The ResultSet. Will return null if the query errored for any reason.
     */
    public static ResultSet getResults(String sqlStatement,Object... vars) {
        if (connection != null) {
            try {

                PreparedStatement statement = connection.prepareStatement(sqlStatement);

                if(vars!=null) { //Add the vars to the statement
                    int i=1;
                    for (Object var : vars) {
                        if(var instanceof Integer)
                            statement.setInt(i++,(int)var);
                        else if(var instanceof String)
                            statement.setString(i++,(String)var);
                        else i++;
                    }
                }
                return statement.executeQuery();
            }catch (SQLException e){
                System.out.println("Could not get results of: " + sqlStatement);
                e.printStackTrace();
            }
        }
        else
            System.out.println("Could not get results of: \"" + sqlStatement +
                    "\" because SQL database isn't connected.");
        return null;
    }

    /**
     * <p>Does processing on a SQL statement/query using a provided processor function.</p>
     * <p>Function is run on each row sequentially.</p>
     * <p>Lambda is used when this function is called to inline code for organizational sake.</p>
     * @param sqlStatement The SQL statement/query
     * @param process The function/lambda to run on each row in a result set.
     * @param vars Optional additional vars. Will replace '?' within the query. Query will likely fail if
     *             the wrong number of vars are provided. Will only accept integers and strings.
     */
    public static void process(String sqlStatement, ResultSetProcessor process, Object... vars) {
        if (connection != null)
        {
            try{
                PreparedStatement statement = connection.prepareStatement(sqlStatement,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                if(vars!=null) {
                    int i=1;
                    for (Object var : vars) {
                        if(var instanceof Integer)
                            statement.setInt(i++,(int)var);
                        else if(var instanceof String)
                            statement.setString(i++,(String)var);
                        else throw  new IllegalArgumentException();
                    }
                }

                ResultSet set = statement.executeQuery();
                if(set!=null) {
                    int i = 0;
                    set.last();
                    int count = set.getRow();
                    set.beforeFirst();
                    while (set.next()) {
                        process.process(set,i++,count);
                    }
                }
                else
                    System.out.println("Could not process command: \"" + sqlStatement + "\" due to results being null");
            }catch (SQLException e){
                System.out.println("Could not process command: " + sqlStatement);
                e.printStackTrace();
            }
        }
        else
        {
            System.out.println("Could not process command: \"" + sqlStatement +
                    "\" because SQL database isn't connected.");
        }
    }

    /**
     * Executes a SQL statement/query that does not require results.
     * @param sqlStatement The statement/query.
     * @param vars Optional additional vars. Will replace '?' within the query. Query will likely fail if
     *             the wrong number of vars are provided. Will only accept integers and strings.
     * @return Returns true if the query executed without known issues. False otherwise.
     */
    public static boolean execute(String sqlStatement, Object... vars) {
        if (connection != null)
            try {
                PreparedStatement statement = connection.prepareStatement(sqlStatement,ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
                if(vars!=null) {
                    int i=1;
                    for (Object var : vars) {
                        if(var instanceof Integer)
                            statement.setInt(i++,(int)var);
                        else if(var instanceof String)
                            statement.setString(i++,(String)var);
                        else i++;
                    }
                }
                statement.execute();
                return true;
            }catch(SQLException e){
                System.out.println("Could not execute statement: " + sqlStatement);
                e.printStackTrace();
            }
        else
            System.out.println("Could not execute statement: \"" + sqlStatement +
                    "\" because SQL database isn't connected.");
        return false;
    }
}