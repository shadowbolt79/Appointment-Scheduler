package scheduler.records;

import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.controllers.CalendarController;
import scheduler.elements.AlertBox;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;

/**
 * Holds records for Customers and functions related to them.
 * @author Ken Butler
 */
public record Customer(int id, String name, String address, String postal_code, String phone_number,
                       ZonedDateTime create_date, String created_by, ZonedDateTime last_update, String last_updated_by,
                       Divisions division) {
    //Holds all known Customers so we don't have to keep getting them from the database.
    private static final HashMap<Integer, Customer> customers = new HashMap<>();

    /**
     * Placeholder customer to add a Add New to the customer menu.
     */
    public static Customer addNew = new Customer(-1,DefaultLocale.translate("add_new"),"","","",
            null,"",null,"",new Divisions(-1,"",new Country(-1,"")));

    /**
     * Gets a customer by ID. Returns null if it can't find the customer in the database.
     * @param customer_id The customer's ID.
     * @return The customer.
     */
    public static Customer get(int customer_id) {
        if(customer_id<0)
            return null;
        if(customers.containsKey(customer_id))
            return customers.get(customer_id);
        ResultSet rs = JDBC.getResults("SELECT * from customers WHERE Customer_ID="+customer_id+";");
        if(rs!=null){
            try {
                if (rs.next()) {
                    Customer customer = new Customer(customer_id, rs.getString(2),
                            rs.getString(3), rs.getString(4),rs.getString(5),
                            DefaultLocale.userDateTime(rs.getTimestamp(6)),rs.getString(7),
                            DefaultLocale.userDateTime(rs.getTimestamp(8)),rs.getString(9),
                            Divisions.get(rs.getInt(10)));
                    customers.put(customer_id, customer);
                    return customer;
                }
            }catch(SQLException ignored){}
        }
        return null;
    }

    /**
     * Returns a list of all customers.
     * @return The List of Customers.
     */
    public static Collection<Customer> getCustomers() {
        JDBC.process("SELECT * from customers",(rs,row,count)->{
            int i = rs.getInt(1);
            if(!customers.containsKey(i)||customers.get(i).last_update.isBefore(DefaultLocale.userDateTime(rs.getTimestamp(8))))
                customers.put(i,new Customer(i, rs.getString(2),
                        rs.getString(3), rs.getString(4),rs.getString(5),
                        DefaultLocale.userDateTime(rs.getTimestamp(6)),rs.getString(7),
                        DefaultLocale.userDateTime(rs.getTimestamp(8)),rs.getString(9),
                        Divisions.get(rs.getInt(10))));
        });
        return customers.values();
    }

    /**
     * Updates a customer in the database and returns the updated record.
     * @param name The Customer's Name.
     * @param address The Customer's Address.
     * @param postal_code The Customer's Postal Code.
     * @param phone_number The Customer's Phone Number.
     * @param division The Customer's {@link Divisions}
     * @return The updated Customer. Will return null if there was an error.
     */
    public Customer update(String name, String address, String postal_code, String phone_number, Divisions division) {
        if(!this.name.equals(name)||!this.address.equals(address)||!this.postal_code.equals(postal_code)||!this.phone_number.equals(phone_number)||!this.division.equals(division)) {
            if (JDBC.execute("UPDATE customers SET Customer_Name=?, Address=?, Postal_Code=?, Phone=?, Last_Update=NOW(), Last_Updated_By=?, Division_ID=? WHERE Customer_ID=?;",
                    name, address, postal_code, phone_number, User.current.username(), division.id(), this.id)) {
                Customer c = new Customer(id, name, address, postal_code, phone_number, create_date, created_by, ZonedDateTime.now(), User.current.username(), division);
                customers.put(id,c);
                return c;
            }
            else
                return null;
        }
        return this;
    }

    /**
     * <p>Deletes a customer from the database, as well as any Appointments that belonged to the customer.</p>
     * <p>Will ask for confirmation before deleting anything.</p>
     * @param callback Optional callback to run after delete operation was accepted by user and customers/appointments
     *                 are deleted.
     */
    public void delete(Runnable callback){
        ResultSet rs = JDBC.getResults("SELECT Count(Appointment_ID) FROM appointments WHERE Customer_ID="+id+";");
        try{
            if(rs!=null) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    StringBuilder builder = new StringBuilder(DefaultLocale.translate(count>0?"delete_confirmation_appointments":"delete_confirmation").replace("%1%",name).replace("%2%",String.valueOf(count)));
                    if(count>0){
                        try{
                            ResultSet set = JDBC.getResults("SELECT Appointment_ID, Title, Type, Start FROM appointments WHERE Customer_ID="+id+";");
                            if(set!=null) {
                                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                                while (set.next()) {
                                    builder.append('\n').append(DefaultLocale.translate("id")).append(": ").append(set.getInt(1))
                                            .append(", ").append(DefaultLocale.translate("appointment_title")).append(": ").append(set.getString(2))
                                            .append(", ").append(DefaultLocale.translate("type")).append(": ").append(set.getString(3))
                                            .append(", ").append(DefaultLocale.translate("date")).append(": ").append(DefaultLocale.userDateTime(set.getTimestamp(4)).format(dtf));
                                }
                            }
                        }catch(SQLException ignored){}
                    }
                    AlertBox.show(DefaultLocale.translate(count>0?"warning":"confirmation"), builder.toString(),()->{
                        if(count <= 0 || JDBC.execute("DELETE FROM appointments WHERE Customer_ID=" + id + ";")) {
                            if(count>0) CalendarController.INSTANCE.refresh_appointments();
                            if (JDBC.execute("DELETE FROM customers WHERE Customer_ID=" + id + ";")) {
                                customers.remove(id);
                                if(callback!=null)
                                    callback.run();
                            }
                        }
                    });
                }
            }
        }catch(SQLException ignored){}
    }

    /**
     * Creates a new customer in the database, and returns the record.
     * @param name The Customer's Name.
     * @param address The Customer's Address.
     * @param postal_code The Customer's Postal Code.
     * @param phone_number The Customer's Phone Number.
     * @param division The Customer's {@link Divisions}.
     * @return The new customer. Will return null if there was an error.
     */
    public static Customer createNew(String name, String address, String postal_code, String phone_number, Divisions division) {
        if(User.current==null)return null;

        if(JDBC.execute("INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) VALUES (?, ?, ?, ?, NOW(), ?, NOW(), ?, ?)",
                name, address, postal_code, phone_number, User.current.username(), User.current.username(), division.id())) {
            ResultSet set = JDBC.getResults("SELECT Customer_ID, Create_Date FROM customers WHERE Customer_ID=(SELECT MAX(Customer_ID) FROM customers WHERE Created_By=?);",User.current.username());
            try {
                if (set != null && set.next()) {
                    int i = set.getInt(1);
                    ZonedDateTime time = set.getTimestamp(2).toLocalDateTime().atZone(DefaultLocale.zone);
                    Customer c = new Customer(i,name,address,postal_code,phone_number,time,User.current.username(), time, User.current.username(), division);
                    customers.put(i,c);
                    return c;
                }
            }
            catch(SQLException ignored){}
        }

        return null;
    }

    /**
     * Overridden to correctly display information in a TableView.
     * @return Customer formatted to display as 'Name'.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * <p>Overridden to simplify checking if one customer is equal to another.</p>
     * <p>Will check against Customer ID, and can accept either an integer or another Customer.</p>
     * @param obj Customer or Integer to check against.
     * @return Returns true if Customer ID matches given object.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Integer)
            return id== (Integer) obj;
        if(obj instanceof Customer)
            return id==((Customer)obj).id;
        return false;
    }
}
