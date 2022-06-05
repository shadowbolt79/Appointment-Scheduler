package scheduler.controllers;

import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.records.User;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>Initializes translations and provides controls for the Side Menu on several of the scenes.</p>
 * <p>Provides information on future appointments and a timer in case an appointment is within 15 minutes. Will
 * alert the user if they're within time.</p>
 * <p>Uses static properties to sync stats across the different scenes without having to rerun code.</p>
 */
public class SideMenuController implements User.UserListener {
    /**
     * Static Instance of the controller for referencing in other controllers.
     */
    public static SideMenuController INSTANCE;

    @FXML Label l_user_name, l_num_appointments, l_next_appointment, l_num_clients;
    @FXML ChoiceBox<User> cb_user_select;
    private boolean isAdmin = false;
    private static User currentUser = null;

    private final static SimpleIntegerProperty numAppointments = new SimpleIntegerProperty(0);
    private final static SimpleIntegerProperty numClients = new SimpleIntegerProperty(0);
    private final static SimpleStringProperty nextAppointment = new SimpleStringProperty("");

    private static boolean checkAlarm = false;

    private static final int SECONDS_PER_DAY = 86_400;
    private static final int SECONDS_PER_HOUR = 3600;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int FIFTEEN_MINUTES = 900;

    private static ZonedDateTime nextAppointmentStart, nextAppointmentEnd;
    private static int nextAppointmentID = -1;

    private static long lastCall = System.nanoTime();

    //Timer used to animate amount of time left to next appointment, or track
    private final static AnimationTimer timer=new AnimationTimer() {
        @Override
        public void handle(final long now) {
            if (now > lastCall + 1_000_000_000L) {
                lastCall = now;
                int totalSeconds;
                StringBuilder sb;
                boolean isOngoing = false;
                if (nextAppointmentStart != null && nextAppointmentEnd != null) {
                    if (nextAppointmentStart.isAfter(ZonedDateTime.now())) {
                        //Unlikely anyone is going to set an appointment over 68 years into the future.
                        totalSeconds = (int) Duration.between(ZonedDateTime.now(), nextAppointmentStart).toSeconds();

                        if (totalSeconds > FIFTEEN_MINUTES) {
                            nextAppointment.setValue(DefaultLocale.translate("no_appointment"));
                            return;
                        }

                        sb = new StringBuilder(DefaultLocale.translate("next_appointment"));

                    } else if (nextAppointmentEnd.isAfter(ZonedDateTime.now())) {
                        isOngoing = true;
                        totalSeconds = (int) Duration.between(ZonedDateTime.now(), nextAppointmentEnd).toSeconds();
                        sb = new StringBuilder(DefaultLocale.translate("ongoing_appointment"));
                    } else {
                        refreshAppointments();
                        checkAlarm = false;
                        return;
                    }
                } else {
                    nextAppointment.setValue(DefaultLocale.translate("no_appointment"));
                    checkAlarm = false;
                    timer.stop();
                    return;
                }

                if (!checkAlarm) {
                    checkAlarm = true;
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setContentText(DefaultLocale.translate("upcoming_alarm")
                            .replace("%1%", Integer.toString(nextAppointmentID))
                            .replace("%2%", nextAppointmentStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a")))
                    );
                    alert.show();
                }

                //int days = totalSeconds / SECONDS_PER_DAY;
                int seconds = totalSeconds % SECONDS_PER_DAY;
                int hours = seconds / SECONDS_PER_HOUR;
                seconds = seconds % SECONDS_PER_HOUR;
                int minutes = seconds / SECONDS_PER_MINUTE;
                seconds = seconds % SECONDS_PER_MINUTE;

                if (isOngoing && totalSeconds > SECONDS_PER_HOUR)
                    sb.append(String.format("%02d", hours)).append(':');

                sb.append(String.format("%02d", minutes)).append(':').append(String.format("%02d", seconds));

                sb.append("\n").append(DefaultLocale.translate("id")).append(": ").append(nextAppointmentID).append("\n(")
                        .append(nextAppointmentStart.format(DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a"))).append(")");

                nextAppointment.setValue(sb.toString());
            }
        }
    };

    /**
     * Gets current user. If admin, they can choose which user they are effectively.
     * @return The current, or admin chosen user.
     */
    public User getCurrentUser(){
        if(isAdmin&&currentUser!=null)
            return currentUser;
        return User.current;
    }

    /**
     * <p>Used by Edit/New Appointment scene to set the admins chosen user to the appointment's user.</p>
     * <p>If the admin changes the selected user, they can assign the appointment to someone new.</p>
     * @param user The user
     */
    public void setCurrentUser(User user){
        if(isAdmin) {
            if (!cb_user_select.getItems().contains(user)) {
                cb_user_select.getItems().clear();
                cb_user_select.getItems().add(User.ALL);
                cb_user_select.getItems().addAll(User.getUsers());
                if (cb_user_select.getItems().contains(user)) {
                    cb_user_select.setValue(user);
                    currentUser=user;
                }
            } else {
                cb_user_select.setValue(user);
                currentUser=user;
            }
            User.notifyListeners();
        }
    }

    /**
     * Default Constructor
     */
    public SideMenuController(){}

    /**
     * Called by JavaFX to initialize the Side Menu controller.
     * <p><p>
     * Provides translations and binds text to the static properties.
     */
    @FXML
    private void initialize() {
        INSTANCE=this;
        User.registerListener(this);

        cb_user_select.setOnAction(actionEvent -> {
            if(cb_user_select.getValue()!=null)
            setCurrentUser(cb_user_select.getValue());
        });

        nextAppointment.setValue(DefaultLocale.translate("no_appointment"));

        l_num_appointments.textProperty().bind(
                Bindings.concat(DefaultLocale.translate("num_appointments"),numAppointments));
        l_num_clients.textProperty().bind(
                Bindings.concat(DefaultLocale.translate("num_clients"),numClients));
        l_next_appointment.textProperty().bind(
                Bindings.concat(nextAppointment));
    }

    /**
     * <p>Called when the user is changed.</p>
     * <p>Used to detect when the admin account is logged in and give them access to the ability to pretend to be other
     * users.</p>
     */
    @Override
    public void onUserChange() {
        isAdmin = User.current!=null&&User.current.isAdmin();

        l_user_name.setVisible(!isAdmin);
        l_user_name.setText(User.current!=null?User.current.username():"not_logged_in");

        cb_user_select.setVisible(isAdmin);

        if(isAdmin) {
            if(cb_user_select.getValue()==null) {
                cb_user_select.getItems().clear();
                cb_user_select.getItems().add(User.ALL);
                cb_user_select.getItems().addAll(User.getUsers());
            }
            cb_user_select.setValue(currentUser==null?User.current:currentUser);
        }
        else if(User.current==null) {
            cb_user_select.setValue(null);
            currentUser=null;
        }
        refreshAppointments();
    }

    /**
     * Refreshes appointment counters and next appointment timer.
     */
    public static void refreshAppointments(){
        if(User.current!=null) {
            JDBC.process("SELECT COUNT(Appointment_ID), COUNT(DISTINCT Customer_ID) FROM appointments WHERE End>=?" + (INSTANCE.getCurrentUser() == User.ALL ? "" : " AND User_ID=?") + ";", (resultSet, row, count) -> {
                numAppointments.set(resultSet.getInt(1));
                numClients.set(resultSet.getInt(2));
            }, DefaultLocale.sqlDateTime(ZonedDateTime.now()), (INSTANCE.getCurrentUser() == User.ALL ? null : INSTANCE.getCurrentUser().id()));
            nextAppointmentStart = null;
            nextAppointmentEnd = null;
            nextAppointmentID = -1;
            JDBC.process("SELECT Appointment_ID, Start, End FROM appointments WHERE End>=?" + (INSTANCE.getCurrentUser() == User.ALL ? "" : " AND User_ID=?") + " ORDER BY Start ASC LIMIT 1;", (resultSet, row, count) -> {
                nextAppointmentID=resultSet.getInt(1);
                nextAppointmentStart=DefaultLocale.userDateTime(resultSet.getTimestamp(2));
                nextAppointmentEnd=DefaultLocale.userDateTime(resultSet.getTimestamp(3));
                timer.start();
            }, DefaultLocale.sqlDateTime(ZonedDateTime.now()), (INSTANCE.getCurrentUser() == User.ALL ? null : INSTANCE.getCurrentUser().id()));
        } else {
            numAppointments.set(0);
            numClients.set(0);
            nextAppointment.setValue(DefaultLocale.translate("no_appointment"));
            timer.stop();
        }
    }
}
