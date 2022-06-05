package scheduler.records;

import scheduler.JDBC;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds records for contacts and functions related to them.
 * @author Ken Butler
 */
public record Contact(int id, String name, String email) {
    private static final HashMap<Integer, Contact> contacts = new HashMap<>();

    /**
     * Returns a single contact via ID.
     * @param contact_id The Contact's ID.
     * @return The Contact, or null if not found.
     */
    public static Contact get(int contact_id) {
        if(contact_id<0)
            return null;
        if(contacts.containsKey(contact_id))
            return contacts.get(contact_id);
        ResultSet rs = JDBC.getResults("SELECT Contact_Name, Email from contacts WHERE Contact_ID="+contact_id+";");
        if(rs!=null){
            try {
                if (rs.next()) {
                    Contact contact = new Contact(contact_id, rs.getString(1), rs.getString(2));
                    contacts.put(contact_id, contact);
                    return contact;
                }
            }catch(SQLException ignored){}
        }
        return null;
    }

    /**
     * Returns a list of all contacts
     * @return The list of contacts.
     */
    public static Collection<Contact> getContacts() {
        JDBC.process("SELECT Contact_ID, Contact_Name, Email from contacts",(rs,row,count)->{
            if(!contacts.containsKey(rs.getInt(1)))
                contacts.put(rs.getInt(1),new Contact(rs.getInt(1), rs.getString(2),
                        rs.getString(3)));
        });
        return contacts.values();
    }

    /**
     * Overrides toString() for the purpose of choice boxes.
     * @return Contact as a string, formated as 'Name (Email)'
     */
    @Override
    public String toString() {
        return name + "(" + email + ")";
    }

    /**
     * Overriden to simplify .equals() checks. Can check both integer and another Contact.
     * @param obj Either an integer or another Contact. Pulls Contact ID from appointments for the check.
     * @return Returns true if Contact ID matches.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Integer)
            return id== (Integer) obj;
        if(obj instanceof Contact)
            return id==((Contact)obj).id;
        return false;
    }

    /**
     * Deletes a contact from the database.
     * @param callback Function to callback when contact is deleted.
     */
    public void delete(Runnable callback) {
        if(JDBC.execute("DELETE FROM contacts WHERE Contact_ID="+id)) {
            contacts.remove(id);
            if(callback!=null)
                callback.run();
        }
    }
}
