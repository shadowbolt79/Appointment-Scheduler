package scheduler.records;

import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.controllers.EditAppointmentController;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * Holds records for appointments and functions related to them.
 * @author Ken Butler
 */
public record Appointment(int appointmentID, String title, String description,
                          String location, String type, ZonedDateTime start,
                          ZonedDateTime end, ZonedDateTime creation_date, String created_by, String last_updated_by,
                          ZonedDateTime last_update, Customer customer, User user,
                          Contact contact) {

    /**
     * Updates the database with new information and returns the updated record.
     * @param appointmentID The Appointment's ID
     * @param title The Appointment's Title
     * @param description The Appointment's description
     * @param location The Appointment's location
     * @param type The Appointment's type
     * @param start The starting time and date of the appointment.
     * @param end The ending time and date of the appointment.
     * @param customer The customer requesting the appointment.
     * @param user The user who owns the appointment.
     * @param contact The appointment's contact info.
     * @return The updated copy of the record. Returns null if there was an error preventing it from doing so.
     */
    public Appointment update(int appointmentID, String title, String description,
                          String location, String type, ZonedDateTime start,
                          ZonedDateTime end, Customer customer,
                          User user, Contact contact) {
        if(this.appointmentID!=appointmentID)
            return null;

        //Check to make sure appointment still exists, if not, recreate it. Someone may have deleted.
        if(EditAppointmentController.INSTANCE.getAppointment()!=null){
            ResultSet rs2 = JDBC.getResults("SELECT Appointment_ID FROM appointments WHERE Appointment_ID="+appointmentID+";");
            try {
                if (rs2 == null || !rs2.next()) {
                    EditAppointmentController.INSTANCE.setAppointment(null, false);
                    return createNew(title,description,location,type,start,end,customer,user,contact);
                }
            }catch (SQLException ignored){
                //Most likely scenario is someone deleted the customer.
                //TODO Show error?
                return null;
            }
        }

        if(!title.equals(this.title)||!description.equals(this.description)||
                !location.equals(this.location)||!type.equals(this.type)||
                !start.equals(this.start)||!end.equals(this.end)||!customer.equals(this.customer)||
                !user.equals(this.user)||!contact.equals(this.contact)
        ) {
            if (JDBC.execute("UPDATE appointments SET Title=?, Description=?, Location=?, Type=?, " +
                    "Start='" + DefaultLocale.sqlDateTime(start) + "', " +
                    "End='" + DefaultLocale.sqlDateTime(end) + "', " +
                    "Last_Update='" + DefaultLocale.sqlDateTime(ZonedDateTime.now()) + "', " +
                    "Last_Updated_By='" + User.current.username() + "', " +
                    "Customer_ID=" + customer.id() + ", " +
                    "User_ID=" + user.id() + ", " +
                    "Contact_ID=" + contact.id() +
                    " WHERE Appointment_ID=" + appointmentID + ";", title, description, location, type)) {
                return new Appointment(this.appointmentID, title, description,
                        location, type, start, end, this.creation_date, this.created_by, User.current.username(),
                        ZonedDateTime.now(), customer, user, contact);
            } else
                return null;
        }
        else
            return this;
    }

    /**
     * Generates a new appointment and returns the created record.
     * @param title The Appointment's Title
     * @param description The Appointment's description
     * @param location The Appointment's location
     * @param type The Appointment's type
     * @param start The starting time and date of the appointment.
     * @param end The ending time and date of the appointment.
     * @param customer The customer requesting the appointment.
     * @param user The user who owns the appointment.
     * @param contact The appointment's contact info.
     * @return The new record of the appointment. Returns null if there was an error preventing it from doing so.
     */
    public static Appointment createNew(String title, String description,
                                        String location, String type, ZonedDateTime start,
                                        ZonedDateTime end, Customer customer, User user,
                                        Contact contact) {
        ZonedDateTime now = ZonedDateTime.now();
        String now2 = DefaultLocale.sqlDateTime(now);
        if (JDBC.execute("INSERT INTO appointments (Title, Description, Location, " +
                "Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, " +
                "Customer_ID, User_ID, Contact_ID) VALUES (?, ?, ?, ?, " +
                "'" + DefaultLocale.sqlDateTime(start) + "', " +
                "'" + DefaultLocale.sqlDateTime(end) + "', " +
                "'" + now2 + "', " +
                "'" + User.current.username() + "', " +
                "'" + now2 + "', " +
                "'" + User.current.username() + "', " +
                customer.id() + ", " +
                user.id() + ", " +
                contact.id() + ");", title, description, location, type)) {
            try {
                ResultSet appointment = JDBC.getResults("SELECT Appointment_ID FROM appointments WHERE " +
                        "Created_By='" + User.current.username() + "' ORDER BY Create_Date DESC LIMIT 1;");
                if (appointment == null) {
                    System.out.println("Appointment null");
                    return null;
                }
                if (appointment.next()) {
                    return new Appointment(appointment.getInt(1), title, description,
                            location, type, start, end, now, User.current.username(), User.current.username(),
                            now, customer, user, contact);
                } else
                    System.out.println("Appointment doesn't have next");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Attempts to delete an appointment from the database. Returns true if it appears it was successful.
     * @return Whether there were no errors in executing the statement.
     */
    public boolean cancelAppointment() {
        return JDBC.execute("DELETE FROM appointments WHERE Appointment_ID=" + appointmentID);
    }

    /**
     * Returns a list of appointments, with optional conditions.
     * @param user If not null, or {@link User#ALL}, then returns only the appointments belonging to this user.
     * @param start If not null, returns only the appointments that end after this start time.
     * @param end If not null, returns only the appointments that start before this end time.
     * @return The list of appointments.
     */
    public static ArrayList<Appointment> getAppointments(User user, ZonedDateTime start, ZonedDateTime end) {
        try{
            String query = "Select * FROM appointments";
            if((user!=User.ALL&&user!=null)||start!=null||end!=null)
            {
                boolean conditions = false;
                query+=" WHERE";
                if(user!=User.ALL&&user!=null) {
                    query += " User_ID="+user.id();
                    conditions=true;
                }
                if(start!=null){
                    query+=(conditions?" AND End>='":" End>='")+DefaultLocale.sqlDateTime(start)+"'";
                    conditions=true;
                }
                if(end!=null){
                    query+=(conditions?" AND Start<='":" Start<='")+DefaultLocale.sqlDateTime(end)+"'";
                }
            }
            query+=" ORDER BY Start;";
            ArrayList<Appointment> appointments = new ArrayList<>();
            JDBC.process(query,(rs,row,c)-> appointments.add(
                    new Appointment(rs.getInt(1),rs.getString(2),
                            rs.getString(3), rs.getString(4), rs.getString(5),
                            DefaultLocale.userDateTime(rs.getTimestamp(6)),
                            DefaultLocale.userDateTime(rs.getTimestamp(7)),
                            DefaultLocale.userDateTime(rs.getTimestamp(8)), rs.getString(9),
                            rs.getString(11), DefaultLocale.userDateTime(rs.getTimestamp(10)),
                            Customer.get(rs.getInt(12)), User.get(rs.getInt(13)),
                            Contact.get(rs.getInt(14)))
            ));
            return appointments;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a list of appointments, with optional conditions. Used specifically for testing overlap in customer
     * appointments.
     * @param customer If not null, then returns only the appointments belonging to this customer.
     * @param start If not null, returns only the appointments that end after this start time.
     * @param end If not null, returns only the appointments that start before this end time.
     * @return The list of appointments.
     */
    public static ArrayList<Appointment> getAppointments(Customer customer, ZonedDateTime start, ZonedDateTime end) {
        try{
            String query = "Select * FROM appointments";
            if(customer!=null||start!=null||end!=null)
            {
                boolean conditions = false;
                query+=" WHERE";
                if(customer!=null) {
                    query += " Customer_ID="+customer.id();
                    conditions=true;
                }
                if(start!=null){
                    query+=(conditions?" AND End>='":" End>='")+DefaultLocale.sqlDateTime(start)+"'";
                    conditions=true;
                }
                if(end!=null){
                    query+=(conditions?" AND Start<='":" Start<='")+DefaultLocale.sqlDateTime(end)+"'";
                }
            }
            query+=" ORDER BY Start;";
            ArrayList<Appointment> appointments = new ArrayList<>();
            JDBC.process(query,(rs,row,c)->
                appointments.add(
                        new Appointment(rs.getInt(1),rs.getString(2),
                                rs.getString(3), rs.getString(4), rs.getString(5),
                                DefaultLocale.userDateTime(rs.getTimestamp(6)),
                                DefaultLocale.userDateTime(rs.getTimestamp(7)),
                                DefaultLocale.userDateTime(rs.getTimestamp(8)), rs.getString(9),
                                rs.getString(11), DefaultLocale.userDateTime(rs.getTimestamp(10)),
                                Customer.get(rs.getInt(12)), User.get(rs.getInt(13)),
                                Contact.get(rs.getInt(14)))
                )
            );
            return appointments;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Overriden to simplify .equals() checks. Can check both integer and another appointment.
     * @param obj Either an integer or another Appointment. Pulls Appointment ID from appointments for the check.
     * @return Returns true if Appointment ID matches.
     */
    @Override
    public boolean equals(Object obj) {
        //Appointment IDs generated by SQL server and should be unique.
        if(obj instanceof Integer)
            return appointmentID==(int)obj;
        if(obj instanceof Appointment)
            return appointmentID==((Appointment) obj).appointmentID;
        return false;
    }
}