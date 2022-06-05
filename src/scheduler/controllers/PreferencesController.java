package scheduler.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import scheduler.DefaultLocale;
import scheduler.JDBC;
import scheduler.Main;
import scheduler.records.*;

import java.io.IOException;
import java.time.ZonedDateTime;

/**
 * Controller for the preference menu. Also contains controls for testing.
 * @author Ken Butler
 */
@SuppressWarnings("ConstantConditions")
public class PreferencesController {
    private static PreferencesController INSTANCE;

    /**
     * The allowed minimum Duration of Appointments. Represents value*5 minutes.
     */
    public static int minAppointmentDuration = 4;

    private static Stage preferencesStage;

    private Customer testingMcGee;
    private Contact testingContact;

    @FXML private AnchorPane ap_pane;
    @FXML private Label l_min_appointment_length, l_test_appointment;
    @FXML private CheckBox cb_testing_mode;
    @FXML private ChoiceBox<String> cb_min_duration;
    @FXML private Button b_5, b_10, b_15, b_20, b_ok;

    /**
     * Loads the preference window stage and assigns the primary window as the owner.
     * @param primaryStage The Primary Window
     */
    public static void load(Stage primaryStage){
        try {
            preferencesStage = new Stage();
            Scene preferencesScene = new Scene(FXMLLoader.load(Main.class.getResource("scenes/Preferences.fxml")), 256, 200);
            preferencesStage.initModality(Modality.APPLICATION_MODAL);
            preferencesStage.initOwner(primaryStage);
            preferencesStage.setScene(preferencesScene);
        }catch (IOException ignored){}
    }

    /**
     * Shows the preferences window
     */
    public static void show(){
        if(preferencesStage!=null)
            preferencesStage.showAndWait();
    }

    /**
     * Translates the scene, sets positions, and provides functionality to the preferences window.
     * <p><p>
     * Also allows the quick creation of test appointments if testing mode is enabled.
     */
    @FXML
    private void initialize() {
        INSTANCE=this;

        l_min_appointment_length.setText(DefaultLocale.translate("min_length"));
        l_test_appointment.setText(DefaultLocale.translate("create_test_appointment"));
        cb_testing_mode.setText(DefaultLocale.translate("enable_testing_mode"));
        preferencesStage.setTitle(DefaultLocale.translate("preferences"));
        preferencesStage.setResizable(false);
        b_5.setText("5 min");
        b_10.setText("10 min");
        b_15.setText("15 min");
        b_20.setText("20 min");
        b_ok.setText(DefaultLocale.translate("ok"));

        l_min_appointment_length.translateXProperty().bind(cb_min_duration.translateXProperty().subtract(l_min_appointment_length.widthProperty()).subtract(10));
        cb_min_duration.translateXProperty().bind(ap_pane.widthProperty().subtract(cb_min_duration.widthProperty()).subtract(10));
        cb_testing_mode.translateXProperty().bind(ap_pane.widthProperty().subtract(cb_testing_mode.widthProperty()).subtract(10));
        b_5.translateXProperty().bind(b_10.translateXProperty().subtract(b_5.widthProperty()).subtract(10));
        b_10.translateXProperty().bind(b_15.translateXProperty().subtract(b_10.widthProperty()).subtract(10));
        b_15.translateXProperty().bind(b_20.translateXProperty().subtract(b_15.widthProperty()).subtract(10));
        b_20.translateXProperty().bind(ap_pane.widthProperty().subtract(b_20.widthProperty()).subtract(10));
        b_ok.translateXProperty().bind(ap_pane.widthProperty().subtract(b_ok.widthProperty()).subtract(10));
        b_ok.translateYProperty().bind(ap_pane.heightProperty().subtract(b_ok.heightProperty()).subtract(10));

        for(int i = 1; i < 25;i++)
            cb_min_duration.getItems().add((i*5) + " min");

        cb_min_duration.getSelectionModel().select(minAppointmentDuration-1);
        cb_min_duration.setOnAction(actionEvent -> minAppointmentDuration=cb_min_duration.getSelectionModel().getSelectedIndex()+1);

        b_5.setOnAction(actionEvent -> {
            if(User.current!=null&&getTestingContact()!=null&&getTestingCustomer()!=null)
                CalendarController.INSTANCE.addAppointment(Appointment.createNew("Alarm Test - 5 Minute", "Alarm Testing Appointment", "undefined", "Test", ZonedDateTime.now().plusMinutes(5), ZonedDateTime.now().plusMinutes(5).plusMinutes(minAppointmentDuration* 5L),testingMcGee,SideMenuController.INSTANCE.getCurrentUser(), testingContact));
        });
        b_10.setOnAction(actionEvent -> {
            if(User.current!=null&&getTestingContact()!=null&&getTestingCustomer()!=null)
                CalendarController.INSTANCE.addAppointment(Appointment.createNew("Alarm Test - 10 Minute", "Alarm Testing Appointment", "undefined", "Test", ZonedDateTime.now().plusMinutes(10), ZonedDateTime.now().plusMinutes(10).plusMinutes(minAppointmentDuration* 5L),testingMcGee,SideMenuController.INSTANCE.getCurrentUser(), testingContact));
        });
        b_15.setOnAction(actionEvent -> {
            if(User.current!=null&&getTestingContact()!=null&&getTestingCustomer()!=null)
                CalendarController.INSTANCE.addAppointment(Appointment.createNew("Alarm Test - 15 Minute", "Alarm Testing Appointment", "undefined", "Test", ZonedDateTime.now().plusMinutes(15), ZonedDateTime.now().plusMinutes(15).plusMinutes(minAppointmentDuration* 5L),testingMcGee,SideMenuController.INSTANCE.getCurrentUser(), testingContact));
        });
        b_20.setOnAction(actionEvent -> {
            if(User.current!=null&&getTestingContact()!=null&&getTestingCustomer()!=null)
                CalendarController.INSTANCE.addAppointment(Appointment.createNew("Alarm Test - 20 Minute", "Alarm Testing Appointment", "undefined", "Test", ZonedDateTime.now().plusMinutes(20), ZonedDateTime.now().plusMinutes(20).plusMinutes(minAppointmentDuration* 5L),testingMcGee,SideMenuController.INSTANCE.getCurrentUser(), testingContact));
        });
        l_test_appointment.visibleProperty().bind(cb_testing_mode.selectedProperty());
        b_5.visibleProperty().bind(cb_testing_mode.selectedProperty());
        b_10.visibleProperty().bind(cb_testing_mode.selectedProperty());
        b_15.visibleProperty().bind(cb_testing_mode.selectedProperty());
        b_20.visibleProperty().bind(cb_testing_mode.selectedProperty());

        b_ok.setOnAction(actionEvent -> preferencesStage.hide());

        cb_testing_mode.setOnAction(actionEvent -> {
            if(!cb_testing_mode.isSelected()){
                if(testingMcGee!=null) {
                    cb_testing_mode.selectedProperty().setValue(true);
                    testingMcGee.delete(() -> {
                        testingMcGee = null;
                        if (testingContact != null)
                            testingContact.delete(() -> testingContact = null);
                        cb_testing_mode.selectedProperty().setValue(false);
                    });
                }
            }
            else {
                getTestingCustomer();
                getTestingContact();
            }
            EditAppointmentController.INSTANCE.onUserChange();
            EditAppointmentController.INSTANCE.calibrateTimes();
        });
    }

    /**
     * <p>Returns true if testing mode is enabled.</p>
     * <p>Used primarily when testing for times outside of work hours. Testing mode disables protections.</p>
     * @return If testing mode is enabled.
     */
    public static boolean isTestingMode(){
        return INSTANCE.cb_testing_mode.selectedProperty().get();
    }

    /**
     * Gets the testing customer for use of creating test appointments. Creates it if it doesn't exist.
     * @return The Test Customer
     */
    private Customer getTestingCustomer() {
        if(testingMcGee!=null) return testingMcGee;
        try{
            JDBC.process("SELECT Customer_ID FROM customers WHERE Customer_Name=? AND Address=? AND Postal_Code=?;",(resultSet, row, count) -> testingMcGee = Customer.get(resultSet.getInt(1)), "Testing McGee", "123 Test Street", "12345");
        }catch (Exception ignored) {}
        if(testingMcGee==null){
            testingMcGee = Customer.createNew("Testing McGee", "123 Test Street", "12345", "123-456-7890", Divisions.get(43));
        }
        return testingMcGee;
    }

    /**
     * Creates the test contact for use of creating test appointments. Creates it if it doesn't exist.
     * @return The Test Contact
     */
    private Contact getTestingContact() {
        if(testingContact!=null)return testingContact;

        try{
            JDBC.process("SELECT Contact_ID FROM contacts WHERE Contact_Name=? AND Email=?;",((resultSet, row, count) -> testingContact = Contact.get(resultSet.getInt(1))), "Testing McGee", "toomuch@testing.com");
        }catch (Exception ignored) {}
        if(testingContact==null){
            if(JDBC.execute("INSERT INTO contacts (Contact_Name, Email) VALUES (?, ?);","Testing McGee", "toomuch@testing.com")){
                JDBC.process("SELECT Contact_ID FROM contacts WHERE Contact_Name=? AND Email=?;",((resultSet, row, count) -> testingContact = Contact.get(resultSet.getInt(1))), "Testing McGee", "toomuch@testing.com");
            }
        }

        return testingContact;
    }
}