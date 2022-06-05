package scheduler.records;

import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds records for Users and functions related to them.
 * @author Ken Butler
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public record User(String username, int id) {

    /**
     * The current user. Null means not logged in.
     */
    public static User current = null;

    /**
     * Fake user representing all users. Used for admins.
     */
    public static User ALL = new User("All",-1);
    private static final HashMap<Integer, User> users = new HashMap<>();

    /**
     * Enables different scenes to listen for user changes.
     */
    public interface UserListener {
        //TODO: Replace User.current with Observable Object?

        /**
         * Called when {@link User#current} is updated.
         */
        void onUserChange();
    }

    private static final ArrayList<UserListener> listeners = new ArrayList<>();

    /**
     * Adds a listener to the list.
     * @param listener The listener to add.
     */
    public static void registerListener(UserListener listener)
    {
        listeners.add(listener);
    }

    /**
     * Notifies all listeners via function listed in the UserListener interface.
     */
    public static void notifyListeners() {
        for (UserListener listener:listeners) {
            listener.onUserChange();
        }
    }

    /**
     * Gets a User by its ID.
     * @param user_id The User's ID.
     * @return The User. Returns null if user is not found.
     */
    public static User get(int user_id) {
        if(user_id<0)
            return ALL;
        if(users.containsKey(user_id))
            return users.get(user_id);
        ResultSet rs = JDBC.getResults("SELECT User_Name from users WHERE User_ID="+user_id+";");
        if(rs!=null){
            try {
                if (rs.next()) {
                    User user = new User(rs.getString(1), user_id);
                    users.put(user_id, user);
                    return user;
                }
            }catch(SQLException ignored){}
        }
        return null;
    }

    /**
     * Gets a list of all Users. Primarily needed for administrative purposes.
     * @return The list of all users.
     */
    public static Collection<User> getUsers() {
        JDBC.process("SELECT * from users ORDER BY User_Name",(set,row,count)->{
            if(!users.containsKey(set.getInt(1)))
                users.put(set.getInt(1),new User(set.getString(2),set.getInt(1)));
        });
        return users.values();
    }

    /**
     * Attempts to login using given username and password.
     * @param username The username
     * @param password The password
     * @return Returns true if the attempt was successful.
     */
    public static boolean attemptLogin(String username, String password) {
        try {
            ResultSet set = JDBC.getResults("SELECT * from users WHERE User_Name='"+username+"' AND Password='"+password+"';");
            if(set==null)
                return false;
            if(set.next())
            {
                if(users.containsKey(set.getInt(1)))
                    current=users.get(set.getInt(1));
                else{
                    current = new User(set.getString(2),set.getInt(1));
                    users.put(current.id,current);
                }
                notifyListeners();
                Main.setScene(Main.calender_scene);

                return true;
            }
            else
                return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Logs out the current user and returns to the login scene.
     */
    public static void logout() {
        if(current!=null) {
            try {
                File file = new File("login_activity.txt");
                if (!file.exists())
                    file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("[MM/dd/yyyy - HH:mm:ss]: ")).getBytes(StandardCharsets.UTF_8));
                fos.write((current.username + DefaultLocale.translate("logged_out")).getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignored) { }
            current = null;
        }
        Main.setScene(Main.login_scene);
        notifyListeners();
    }

    /**
     * <p>Checks to see if current user is admin.</p>
     * <p>Currently set to return false as no users should be admin.</p>
     * @return Returns true if username is "admin"
     */
    public boolean isAdmin() {
        //Oops.
        //return username.equals("admin");
        return false;
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Integer)
            return id== (Integer) obj;
        if(obj instanceof User)
            return id==((User)obj).id;
        return false;
    }
}
