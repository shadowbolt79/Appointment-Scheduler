package scheduler.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import scheduler.DefaultLocale;
import scheduler.Main;
import scheduler.records.User;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Controls the navigation menu on top of all the scenes.
 *
 * @author Ken Butler
 */
public class MenuController implements User.UserListener {

    //FXML field initialized by JavaFX
    @FXML private Menu m_file;
    @FXML private Menu m_help;
    @FXML private Menu m_navigation;
    @FXML private Menu m_admin;
    @FXML private MenuItem mi_new_appointment;
    @FXML private MenuItem mi_duplicate_appointment;
    @FXML private MenuItem mi_edit_appointment;
    @FXML private MenuItem mi_delete_appointment;
    @FXML private MenuItem mi_preferences;
    @FXML private MenuItem mi_logout;
    @FXML private MenuItem mi_quit;

    @FXML private Menu m_reports;
    @FXML private MenuItem mi_customers_by_month;
    @FXML private MenuItem mi_customers_by_country;
    @FXML private MenuItem mi_contact_schedule;

    @FXML private RadioMenuItem rmi_calendar;
    @FXML private RadioMenuItem rmi_new_edit_appointment;
    @FXML private RadioMenuItem rmi_elsewhere;
    @FXML private RadioMenuItem rmi_new_edit_customer;

    @FXML private MenuItem mi_about;
    @FXML private MenuItem mi_new_user;
    @FXML private MenuItem mi_user_list;

    //Due to multiple copies of the menu, watched scene property syncs the current scene.
    private static final SimpleObjectProperty<Scene> sceneProperty = new SimpleObjectProperty<>(null);
    private boolean isUpdating = false;

    /**
     * Default Constructor
     */
    public MenuController(){}

    /**
     * Called by JavaFX to initialize the menu.
     */
    @FXML
    private void initialize() {
        User.registerListener(this);

        m_file.setText(DefaultLocale.translate(m_file.getText()));
        m_help.setText(DefaultLocale.translate(m_help.getText()));
        m_navigation.setText(DefaultLocale.translate(m_navigation.getText()));
        m_admin.setText(DefaultLocale.translate(m_admin.getText()));
        mi_new_appointment.setText(DefaultLocale.translate(mi_new_appointment.getText()));
        mi_duplicate_appointment.setText(DefaultLocale.translate(mi_duplicate_appointment.getText()));
        mi_edit_appointment.setText(DefaultLocale.translate("modify_appointment"));
        mi_delete_appointment.setText(DefaultLocale.translate(mi_delete_appointment.getText()));
        mi_preferences.setText(DefaultLocale.translate(mi_preferences.getText()));
        mi_logout.setText(DefaultLocale.translate(mi_logout.getText()));
        mi_quit.setText(DefaultLocale.translate(mi_quit.getText()));
        rmi_calendar.setText(DefaultLocale.translate(rmi_calendar.getText()));
        rmi_new_edit_appointment.setText(DefaultLocale.translate(rmi_new_edit_appointment.getText()));
        rmi_new_edit_customer.setText(DefaultLocale.translate(rmi_new_edit_customer.getText()));
        rmi_elsewhere.setText(DefaultLocale.translate(rmi_elsewhere.getText()));
        mi_about.setText(DefaultLocale.translate(mi_about.getText()));
        mi_new_user.setText(DefaultLocale.translate(mi_new_user.getText()));
        mi_user_list.setText(DefaultLocale.translate(mi_user_list.getText()));
        m_reports.setText(DefaultLocale.translate(m_reports.getText()));
        mi_customers_by_month.setText(DefaultLocale.translate(mi_customers_by_month.getText()));
        mi_customers_by_country.setText(DefaultLocale.translate(mi_customers_by_country.getText()));
        mi_contact_schedule.setText(DefaultLocale.translate(mi_contact_schedule.getText()));

        mi_edit_appointment.visibleProperty().bind(CalendarController.selectedAppointment.isNotNull());
        mi_duplicate_appointment.visibleProperty().bind(CalendarController.selectedAppointment.isNotNull());

        mi_new_appointment.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.appointment_scene) {
                EditAppointmentController.INSTANCE.setFrom(Main.getScene());
                Main.setScene(Main.appointment_scene);
            }
        });
        mi_edit_appointment.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.appointment_scene) {
                if(CalendarController.selectedAppointment.get()!=null)
                    EditAppointmentController.INSTANCE.setAppointment(CalendarController.selectedAppointment.get());
                EditAppointmentController.INSTANCE.setFrom(Main.getScene());
                Main.setScene(Main.appointment_scene);
            }
        });
        mi_duplicate_appointment.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.appointment_scene) {
                if(CalendarController.selectedAppointment.get()!=null) {
                    EditAppointmentController.INSTANCE.setAppointment(CalendarController.selectedAppointment.get());
                    EditAppointmentController.INSTANCE.setAppointment(null,false);
                }
                EditAppointmentController.INSTANCE.setFrom(Main.getScene());
                Main.setScene(Main.appointment_scene);
            }
        });
        mi_preferences.setOnAction(actionEvent -> PreferencesController.show());
        mi_logout.setOnAction(event -> User.logout());
        mi_quit.setOnAction(event -> System.exit(0));

        //Checkmark different scenes in the menu based on what is selected.
        sceneProperty.addListener(listener -> {
            //isUpdating prevents unnecessary redirects.
            isUpdating=true;
            if(sceneProperty.get()==Main.calender_scene)
                rmi_calendar.setSelected(true);
            else if(sceneProperty.get()==Main.appointment_scene)
                rmi_new_edit_appointment.setSelected(true);
            else if(sceneProperty.get()==Main.customer_scene)
                rmi_new_edit_customer.setSelected(true);
            else
                rmi_elsewhere.setSelected(true);
            isUpdating=false;
        });

        rmi_calendar.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.calender_scene){
                Main.setScene(Main.calender_scene);
            }
        });
        rmi_new_edit_appointment.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.appointment_scene) {
                EditAppointmentController.INSTANCE.setFrom(Main.getScene());
                Main.setScene(Main.appointment_scene);
            }
        });
        rmi_new_edit_customer.setOnAction(actionEvent -> {
            if(!isUpdating&&Main.getScene()!=Main.customer_scene) {
                EditCustomerController.INSTANCE.setFrom(Main.getScene());
                Main.setScene(Main.customer_scene);
            }
        });

        mi_contact_schedule.setOnAction(actionEvent -> ReportController.showContactSchedule());
        mi_customers_by_country.setOnAction(actionEvent -> ReportController.showCountryCount());
        mi_customers_by_month.setOnAction(actionEvent -> ReportController.showTypeMonthReport());

        mi_about.setOnAction(actionEvent -> {
            File file = new File("readme.txt");
            if (file.exists()&&Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().edit(file);
                } catch (IOException ignored) {}
            }
        });
    }

    /**
     * <p>Called when user changes.</p>
     * <p>Hides/Shows sections of the menu based on who is logged in.</p>
     */
    @Override
    public void onUserChange() {
        boolean isAdmin = User.current!=null&&User.current.isAdmin();
        boolean isLoggedIn = User.current!=null;
        m_navigation.setVisible(isLoggedIn);
        mi_logout.setVisible(isLoggedIn);
        mi_new_appointment.setVisible(isLoggedIn);
        m_admin.setVisible(isAdmin);
        m_reports.setVisible(isLoggedIn);
    }

    /**
     * Sets the scene, purely for the cosmetic checkmark in the navigation menu.
     * @param scene The scene.
     */
    public static void setLocation(Scene scene) {
        sceneProperty.set(scene);
    }
}
