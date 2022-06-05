package scheduler.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import scheduler.*;
import scheduler.elements.AlertBox;
import scheduler.interfaces.BackTracker;
import scheduler.records.Appointment;
import scheduler.records.Contact;
import scheduler.records.Customer;
import scheduler.records.User;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * <p>Controller for the Edit/New Appointment Scene.</p>
 * <p>Translates and provides functionality for the scene. Button's functions are provided with lambda for organization's
 * sake. Callback for the Edit/Select Customer Scene is also a lambda for the same reason.</p>
 * @author Ken Butler
 */
@SuppressWarnings("unused")
public class EditAppointmentController extends BackTracker<Appointment> implements User.UserListener {
    /**
     * Static Instance of the controller for referencing in other controllers.
     */
    public static EditAppointmentController INSTANCE;

    /**
     * null means creating a new appointment, otherwise what appointment is being edited.
     */
    private Appointment appointment = null;

    //FXML variables filled by JavaFX
    @FXML private Label l_appointment_title, l_edit_appointment, l_description, l_location, l_start_time, l_end_time,
            l_customer, l_user, l_contact, l_type, l_appointment_id, l_errors;

    @FXML private ChoiceBox<String> cb_start_hour, cb_end_hour, cb_start_minute, cb_end_minute;
    @FXML private ChoiceBox<Contact> cb_contact;
    @FXML private  ChoiceBox<Customer> cb_customer;
    @FXML private DatePicker dp_start;
    @FXML private Button b_ok, b_cancel, b_delete, b_back, b_add_customer;
    @FXML private TextField tf_title, tf_type, tf_user, tf_location;
    @FXML private TextArea ta_description;

    //If these aren't loaded, they may not sync. For some reason.
    @FXML private AnchorPane side_pane;
    @FXML private MenuBar menu_bar;

    // Used to prevent clock onActions from firing inappropriately.
    private static boolean isSetting = false;

    /**
     * Default Constructor.
     */
    public EditAppointmentController(){}

    /**
     * Translates strings, binds positions for window resizing, and provides functionality to buttons.
     */
    @FXML
    private void initialize(){
        User.registerListener(this);
        INSTANCE=this;

        l_appointment_title.setText(DefaultLocale.translate("appointment_title"));
        l_edit_appointment.setText(DefaultLocale.translate("modify_appointment")+": ");
        l_description.setText(DefaultLocale.translate("description")+": ");
        l_location.setText(DefaultLocale.translate("location")+": ");
        l_start_time.setText(DefaultLocale.translate("start_time")+": ");
        l_end_time.setText(DefaultLocale.translate("end_time")+": ");
        l_customer.setText(DefaultLocale.translate("customer")+": ");
        l_user.setText(DefaultLocale.translate("user")+": ");
        l_contact.setText(DefaultLocale.translate("contact")+": ");
        l_type.setText(DefaultLocale.translate("type")+": ");

        b_ok.setText(DefaultLocale.translate("ok"));
        b_cancel.setText(DefaultLocale.translate("cancel"));
        b_back.setText(DefaultLocale.translate("back"));
        b_delete.setText(DefaultLocale.translate("delete"));

        //Prevent text fields from being longer than 50 characters.
        tf_type.setOnKeyTyped(keyEvent -> {
            if(tf_type.getText().length()>=50) {
                int c = tf_type.getCaretPosition();
                if(c>0&&c<50)
                    tf_type.setText(tf_type.getText(0, c-1)+tf_type.getText(c--, 51));
                else
                    tf_type.setText(tf_type.getText(0, 50));
                tf_type.positionCaret(c);
            }
        });
        tf_title.setOnKeyTyped(keyEvent -> {
            if(tf_title.getText().length()>=50) {
                int c = tf_title.getCaretPosition();
                if(c>0&&c<50)
                    tf_title.setText(tf_title.getText(0, c-1)+tf_title.getText(c--, 51));
                else
                    tf_title.setText(tf_title.getText(0, 50));
                tf_title.positionCaret(c);
            }
        });
        tf_location.setOnKeyTyped(keyEvent -> {
            if(tf_location.getText().length()>=50) {
                int c = tf_location.getCaretPosition();
                if(c>0&&c<50)
                    tf_location.setText(tf_location.getText(0, c-1)+tf_location.getText(c--, 51));
                else
                    tf_location.setText(tf_location.getText(0, 50));
                tf_location.positionCaret(c);
            }
        });
        ta_description.setOnKeyTyped(keyEvent -> {
            if(ta_description.getText().length()>=50) {
                int c = ta_description.getCaretPosition();
                if(c>0&&c<50)
                    ta_description.setText(ta_description.getText(0, c-1)+ta_description.getText(c--, 51));
                else
                    ta_description.setText(ta_description.getText(0, 50));
                ta_description.positionCaret(c);
            }
        });

        b_ok.setOnAction(actionEvent -> {
            //OK button first checks to make sure entered values are valid,
            //then saves the appointment.
            //Check to make sure values aren't null

            StringBuilder errors = new StringBuilder();
            boolean error = false;

            if(tf_title.getText()==null||tf_title.getText().length()<3){
                errors.append(DefaultLocale.translate("title_empty")).append('\n');
                tf_title.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                error=true;
            }
            else tf_title.setStyle("");

            if(tf_type.getText()==null||tf_type.getText().length()<3){
                errors.append(DefaultLocale.translate("type_empty")).append('\n');
                tf_type.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                error=true;
            }
            else tf_type.setStyle("");

            if(dp_start.getValue()==null){
                errors.append(DefaultLocale.translate("date_empty")).append('\n');
                dp_start.setStyle("-fx-focus-color: red; -fx-border-color: red; -fx-border-width: 1 1 1 1;");
                error=true;
            }
            else dp_start.setStyle("");


            if(cb_start_hour.getValue()==null||cb_start_hour.getValue().equals("")||
                    cb_start_minute.getValue()==null||cb_start_minute.getValue().equals("")||
                    cb_end_hour.getValue()==null||cb_end_hour.getValue().equals("")||
                    cb_end_minute.getValue()==null||cb_end_minute.getValue().equals("")){
                errors.append(DefaultLocale.translate("time_empty")).append('\n');
                cb_start_hour.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                cb_start_minute.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                cb_end_minute.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                cb_end_hour.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                error=true;
            }
            //No need to clear style here, checked again later in code.

            if(cb_customer.getValue()==null){
                errors.append(DefaultLocale.translate("customer_empty")).append('\n');
                cb_customer.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                error=true;
            }
            else{
                cb_customer.setStyle("");
            }

            if(cb_contact.getValue()==null){
                errors.append(DefaultLocale.translate("contact_empty")).append('\n');
                cb_contact.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                error=true;
            }
            else{
                cb_contact.setStyle("");
            }

            if(tf_location.getText()==null||tf_location.getText().length()<3){
                errors.append(DefaultLocale.translate("appointment_location_empty")).append('\n');
                tf_location.setStyle("-fx-text-box-border: red; -fx-focus-color: red;");
                error=true;
            }
            else tf_location.setStyle("");

            if(error){
                l_errors.setText(errors.toString());
                return;
            }

            l_errors.setText("");

            //Get selected time
            String[] s = cb_start_hour.getValue().split("\\s+");
            int hour = Integer.parseInt(s[0]);
            if(hour==12) {
                if (s[1].equals("AM"))
                    hour = 0;
            }
            else if(s[1].equals("PM"))
                hour+=12;
            ZonedDateTime start = dp_start.getValue().atStartOfDay(DefaultLocale.zone)
                    .withHour(hour).withMinute(Integer.parseInt(cb_start_minute.getValue()));
            s = cb_end_hour.getValue().split("\\s+");
            hour = Integer.parseInt(s[0]);
            if(hour==12){
                if(s[1].equals("AM"))
                    hour=0;
            }
            else if(s[1].equals("PM"))
                hour+=12;
            ZonedDateTime end = start.withHour(hour).withMinute(Integer.parseInt(cb_end_minute.getValue()));

            //Check if appointment collides with time for customer.

            ArrayList<Appointment> currentAppointments = Appointment.getAppointments(cb_customer.getValue(),start,end);
            if(currentAppointments!=null&&!currentAppointments.isEmpty()) {
                ZonedDateTime minStart = null;
                ZonedDateTime maxEnd = null;
                for (Appointment appointment1 : currentAppointments) {
                    if (!appointment1.equals(appointment)) {
                        if (minStart == null || minStart.isAfter(appointment1.start()))
                            minStart = appointment1.start();
                        if (maxEnd == null || maxEnd.isBefore(appointment1.end()))
                            maxEnd = appointment1.end();
                    }
                }
                if (minStart != null) {
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
                    errors.append(DefaultLocale.translate("time_collision")
                            .replace("%1%", minStart.format(dtf))
                            .replace("%2%", maxEnd.format(dtf)));
                    l_errors.setText(errors.toString());
                    cb_start_hour.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                    cb_start_minute.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                    cb_end_minute.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                    cb_end_hour.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
                    return;
                }
            }
            cb_start_hour.setStyle("");
            cb_start_minute.setStyle("");
            cb_end_minute.setStyle("");
            cb_end_hour.setStyle("");

            //Checking complete, move forward with save.

            User u = SideMenuController.INSTANCE.getCurrentUser();
            if (u.equals(User.ALL))
                u = User.current;

            if (appointment == null) {
                appointment = Appointment.createNew(tf_title.getText(), ta_description.getText(),
                        tf_location.getText(), tf_type.getText(), start,
                        end, cb_customer.getValue(), u, cb_contact.getValue());
                if (appointment != null) {
                    CalendarController.INSTANCE.addAppointment(appointment);
                    goBack(appointment);
                } else
                    System.out.println("Unable to save appointment");
            } else {
                Appointment a = appointment.update(appointment.appointmentID(), tf_title.getText(), ta_description.getText(),
                        tf_location.getText(), tf_type.getText(), start, end,
                        cb_customer.getValue(), User.current.isAdmin()?u:appointment.user(), cb_contact.getValue());
                if (a != null) {
                    CalendarController.INSTANCE.updateAppointment(appointment, a);
                    goBack(a);
                } else {
                    System.out.println("Unable to save update to appointment: " + appointment.appointmentID());
                }
            }
        });
        b_cancel.setOnAction(actionEvent -> goBack(null));
        b_back.setOnAction(actionEvent -> goBack(null));
        b_delete.setOnAction(actionEvent -> AlertBox.show(DefaultLocale.translate("warning"),DefaultLocale.translate("cancel_confirmation").replace("%1%",Integer.toString(appointment.appointmentID())).replace("%2%", appointment.type()).replace("%3%",appointment.customer().name()),()->{
            if(appointment!=null) {
                if(appointment.cancelAppointment())
                    CalendarController.INSTANCE.removeAppointment(appointment);
            }
            setAppointment(null);
            goBack(null);
        }));
        b_add_customer.setOnAction(actionEvent -> {
            EditCustomerController.INSTANCE.setFrom(Main.appointment_scene,customer -> {
                cb_customer.getItems().clear();
                cb_customer.getItems().addAll(Customer.getCustomers());
                if(customer!=null){
                    cb_customer.setValue(customer);
                }
            });
            Main.setScene(Main.customer_scene);
            EditCustomerController.INSTANCE.setCustomer(cb_customer.getValue());
        });

        //Binds time selection so that ending time cannot be set before start time.
        //Also ensures that time is within work hours unless TestingMode is enabled in preferences.
        calibrateTimes();
        cb_start_hour.setOnAction(actionEvent -> {
            if(isSetting)
                return;
            isSetting=true;
            if(cb_start_minute.getValue()==null)
                cb_start_minute.setValue("00");
            int minEnd = cb_start_hour.getItems().indexOf(cb_start_hour.getValue())*12+
                    cb_start_minute.getItems().indexOf(cb_start_minute.getValue())+ PreferencesController.minAppointmentDuration;
            if(!PreferencesController.isTestingMode()&&minEnd>168){
                cb_start_hour.setValue(cb_start_hour.getItems().get(13));
                cb_start_minute.setValue(cb_start_minute.getItems().get(8));
                cb_end_hour.setValue(cb_end_hour.getItems().get(14));
                cb_end_minute.setValue(cb_end_minute.getItems().get(0));
            }
            else if(cb_end_hour.getValue()!=null&&cb_end_minute.getValue()!=null){
                int end = cb_end_hour.getItems().indexOf(cb_end_hour.getValue())*12+
                        cb_end_minute.getItems().indexOf(cb_end_minute.getValue());
                if(end<minEnd){
                    cb_end_hour.setValue(cb_end_hour.getItems().get(minEnd/12));
                    cb_end_minute.setValue(cb_end_minute.getItems().get(minEnd%12));
                }
            }
            else {
                cb_end_hour.setValue(cb_end_hour.getItems().get(minEnd/12));
                cb_end_minute.setValue(cb_end_minute.getItems().get(minEnd%12));
            }
            isSetting=false;
        });
        cb_start_minute.setOnAction(actionEvent -> {
            if(isSetting)
                return;
            isSetting=true;
            if(cb_start_hour.getValue()!=null) {
                int minEnd = cb_start_hour.getItems().indexOf(cb_start_hour.getValue()) * 12 +
                        cb_start_minute.getItems().indexOf(cb_start_minute.getValue()) + PreferencesController.minAppointmentDuration;
                if (!PreferencesController.isTestingMode() && minEnd > 168) {
                    cb_start_hour.setValue(cb_start_hour.getItems().get(13));
                    cb_start_minute.setValue(cb_start_minute.getItems().get(8));
                    cb_end_hour.setValue(cb_end_hour.getItems().get(14));
                    cb_end_minute.setValue(cb_end_minute.getItems().get(0));
                } else if (cb_end_hour.getValue() != null && cb_end_minute.getValue() != null) {
                    int end = cb_end_hour.getItems().indexOf(cb_end_hour.getValue()) * 12 +
                            cb_end_minute.getItems().indexOf(cb_end_minute.getValue());
                    if (end < minEnd) {
                        cb_end_hour.setValue(cb_end_hour.getItems().get(minEnd / 12));
                        cb_end_minute.setValue(cb_end_minute.getItems().get(minEnd % 12));
                    }
                } else {
                    cb_end_hour.setValue(cb_end_hour.getItems().get(minEnd / 12));
                    cb_end_minute.setValue(cb_end_minute.getItems().get(minEnd % 12));
                }
            }
            isSetting=false;
        });
        cb_end_hour.setOnAction(actionEvent -> {
            if(isSetting)
                return;
            isSetting=true;
            if(cb_end_minute.getValue()==null)
                cb_end_minute.setValue("00");
            int maxBeginning = cb_end_hour.getItems().indexOf(cb_end_hour.getValue())*12+
                    cb_end_minute.getItems().indexOf(cb_end_minute.getValue())- PreferencesController.minAppointmentDuration;
            if(maxBeginning<0){
                cb_start_hour.setValue(cb_start_hour.getItems().get(0));
                cb_start_minute.setValue(cb_start_minute.getItems().get(0));
                cb_end_hour.setValue(cb_end_hour.getItems().get(0));
                cb_end_minute.setValue(cb_end_minute.getItems().get(4));
            }
            else if(cb_start_hour.getValue()!=null&&cb_start_minute.getValue()!=null){
                int beginning = cb_start_hour.getItems().indexOf(cb_start_hour.getValue())*12+
                        cb_start_minute.getItems().indexOf(cb_start_minute.getValue());
                if(beginning>maxBeginning){
                    cb_start_hour.setValue(cb_start_hour.getItems().get(maxBeginning/12));
                    cb_start_minute.setValue(cb_start_minute.getItems().get(maxBeginning%12));
                }
            }
            else {
                cb_start_hour.setValue(cb_start_hour.getItems().get(maxBeginning/12));
                cb_start_minute.setValue(cb_start_minute.getItems().get(maxBeginning%12));
            }
            if(cb_end_hour.getSelectionModel().getSelectedIndex()==(PreferencesController.isTestingMode()?24:14))
                cb_end_minute.getSelectionModel().select(0);
            isSetting=false;
        });
        cb_end_minute.setOnAction(actionEvent -> {
            if(isSetting)
                return;
            isSetting=true;
            if(cb_end_hour.getValue()!=null) {
                int maxBeginning = cb_end_hour.getItems().indexOf(cb_end_hour.getValue()) * 12 +
                        cb_end_minute.getItems().indexOf(cb_end_minute.getValue()) - PreferencesController.minAppointmentDuration;
                if (maxBeginning < 0) {
                    cb_start_hour.setValue(cb_start_hour.getItems().get(0));
                    cb_start_minute.setValue(cb_start_minute.getItems().get(0));
                    cb_end_hour.setValue(cb_end_hour.getItems().get(0));
                    cb_end_minute.setValue(cb_end_minute.getItems().get(4));
                } else if (cb_start_hour.getValue() != null && cb_start_minute.getValue() != null) {
                    int beginning = cb_start_hour.getItems().indexOf(cb_start_hour.getValue()) * 12 +
                            cb_start_minute.getItems().indexOf(cb_start_minute.getValue());
                    if (beginning > maxBeginning) {
                        cb_start_hour.setValue(cb_start_hour.getItems().get(maxBeginning / 12));
                        cb_start_minute.setValue(cb_start_minute.getItems().get(maxBeginning % 12));
                    }
                } else {
                    cb_start_hour.setValue(cb_start_hour.getItems().get(maxBeginning / 12));
                    cb_start_minute.setValue(cb_start_minute.getItems().get(maxBeginning % 12));
                }
            }
            if(cb_end_hour.getSelectionModel().getSelectedIndex()==(PreferencesController.isTestingMode()?24:14))
                cb_end_minute.getSelectionModel().select(0);
            isSetting=false;
        });
    }

    /**
     * Calibrates the times in the time choice boxes to hours of operation from EST -> user's local time.
     */
    public void calibrateTimes() {
        isSetting=true;
        cb_start_hour.getItems().clear();
        cb_end_hour.getItems().clear();
        cb_start_minute.getItems().clear();
        cb_end_minute.getItems().clear();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh a");
        ZonedDateTime openingHour = PreferencesController.isTestingMode()?
                ZonedDateTime.parse("2011-12-03 00:00:00 "+DefaultLocale.zone.getId(),DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss z"))
                        .withZoneSameInstant(DefaultLocale.zone):
                ZonedDateTime.parse("2011-12-03 08:00:00 EST",DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss z"))
                        .withZoneSameInstant(DefaultLocale.zone);

        for(int i = 0; i < (PreferencesController.isTestingMode()?24:15); i++)
            cb_start_hour.getItems().add(openingHour.plusHours(i).format(dtf));
        cb_end_hour.getItems().addAll(cb_start_hour.getItems());

        cb_start_minute.getItems().addAll("00","05","10","15","20","25","30","35","40","45","50","55");

        cb_end_minute.getItems().addAll(cb_start_minute.getItems());
        isSetting=false;
    }

    /**
     * <p>Sets the date of the new appointment.</p>
     * <p>Used to default to selected date on the calendar scene.</p>
     * @param time The datetime to take the date from.
     */
    public void setDate(ZonedDateTime time) {
        calibrateTimes();
        dp_start.setValue(time.toLocalDate());
    }

    /**
     * Returns the original pre-edited appointment of the controller.
     * @return The appointment
     */
    public Appointment getAppointment() {return appointment;}

    /**
     * Sets the internal appointment and updates fields so it can be edited.
     * @param appointment The appointment. Can be null.
     */
    public void setAppointment(Appointment appointment){
        setAppointment(appointment,true);
    }

    /**
     * Sets the internal appointment and updates fields so it can be edited.
     * @param appointment The appointment. Can be null.
     * @param updateFields Whether or not to update the fields.
     */
    public void setAppointment(Appointment appointment, boolean updateFields) {
        this.appointment=appointment;
        b_delete.setVisible(appointment!=null);
        if(updateFields) {
            if (appointment != null) {
                l_appointment_id.setText(DefaultLocale.translate("id") + ": " + appointment.appointmentID());
                setDate(appointment.start());

                isSetting = true;
                String start_hour = appointment.start().format(DateTimeFormatter.ofPattern("hh a"));

                if (cb_start_hour.getItems().contains(start_hour)) {
                    cb_start_hour.setValue(start_hour);
                }
                String start_minute = String.format("%02d", ((appointment.start().getMinute() / 5) * 5));
                if (cb_start_minute.getItems().contains(start_minute)) {
                    cb_start_minute.setValue(start_minute);
                }

                String end_hour = appointment.end().format(DateTimeFormatter.ofPattern("hh a"));

                if (cb_end_hour.getItems().contains(end_hour)) {
                    cb_end_hour.setValue(end_hour);
                }
                String end_minute = String.format("%02d", ((appointment.end().getMinute() / 5) * 5));
                if (cb_end_minute.getItems().contains(end_minute)) {
                    cb_end_minute.setValue(end_minute);
                }
                isSetting = false;

                if (!cb_customer.getItems().contains(appointment.customer())) {
                    cb_customer.getItems().clear();
                    cb_customer.getItems().addAll(Customer.getCustomers());
                    if (!cb_customer.getItems().contains(appointment.customer())) {
                        //Missing customer. Possibly deleted by another user. Assume appointment invalid.
                        setAppointment(null, true);
                    } else {
                        cb_customer.setValue(appointment.customer());
                    }
                } else {
                    cb_customer.setValue(appointment.customer());
                }

                if (!cb_contact.getItems().contains(appointment.contact())) {
                    cb_contact.getItems().clear();
                    cb_contact.getItems().addAll(Contact.getContacts());
                    if (!cb_contact.getItems().contains(appointment.contact())) {
                        //Missing contact. Possibly deleted by another user. Assume appointment invalid.
                        setAppointment(null, true);
                    } else {
                        cb_contact.setValue(appointment.contact());
                    }
                } else {
                    cb_contact.setValue(appointment.contact());
                }

                SideMenuController.INSTANCE.setCurrentUser(appointment.user());
                tf_user.setText(appointment.user().username());

                tf_title.setText(appointment.title());
                tf_type.setText(appointment.type());
                ta_description.setText(appointment.description());
                tf_location.setText(appointment.location());
            }
            else {
                l_appointment_id.setText("");
                setDate(ZonedDateTime.now());
                cb_start_hour.setValue(null);
                cb_start_minute.setValue(null);
                cb_end_hour.setValue(null);
                cb_end_minute.setValue(null);

                cb_contact.setValue(null);
                User u = SideMenuController.INSTANCE.getCurrentUser();
                if(u==User.ALL)
                    u=User.current;
                tf_user.setText(u.username());
                cb_customer.setValue(null);

                tf_title.setText("");
                tf_type.setText("");
                ta_description.setText("");
                tf_location.setText("");
            }
        }
        else if (appointment==null) {
            l_appointment_id.setText("");
        }
    }

    /**
     * Fires when a new user logs in or admin changes selected user.
     *
     * Allows admins to create appointments on behalf of a different user.
     */
    @Override
    public void onUserChange() {
        if(User.current!=null) {
            if (appointment == null||User.current.isAdmin()&&SideMenuController.INSTANCE.getCurrentUser()!=User.ALL)
                tf_user.setText(SideMenuController.INSTANCE.getCurrentUser().username());
            else
                tf_user.setText(appointment.user().username());
        }
        Customer c = cb_customer.getValue();
        cb_customer.getItems().clear();
        cb_customer.getItems().addAll(Customer.getCustomers());
        cb_customer.setValue(c);
        Contact c2 = cb_contact.getValue();
        cb_contact.getItems().clear();
        cb_contact.getItems().addAll(Contact.getContacts());
        cb_contact.setValue(c2);
    }
}