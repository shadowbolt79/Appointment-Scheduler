package scheduler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import scheduler.controllers.MenuController;
import scheduler.controllers.PreferencesController;
import scheduler.controllers.ReportController;

/**
 * <p>The main class for the application.</p>
 * <p>Loads the scenes, and is used for switching between them while keeping the size of the window.</p>
 */
@SuppressWarnings("ConstantConditions")
public class Main extends Application {
    /**
     * The Login Scene
     */
    public static Scene login_scene;
    /**
     * The Calendar Scene
     */
    public static Scene calender_scene;
    /**
     * The New/Edit Appointment Scene
     */
    public static Scene appointment_scene;
    /**
     * The New/Edit Customer Scene
     */
    public static Scene customer_scene;

    private static Stage stage;

    /**
     * <p>Loads the scenes and sets the title.</p>
     * <p>Initial scene is set to the login screen.</p>
     * @param primaryStage Stage provided by the application. Stored so scene can be switched.
     */
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        PreferencesController.load(primaryStage);
        ReportController.load(primaryStage);
        calender_scene = new Scene(FXMLLoader.load(getClass().getResource("scenes/Calendar.fxml")), 900,600);
        login_scene = new Scene(FXMLLoader.load(getClass().getResource("scenes/Login.fxml")), 900,600);
        appointment_scene = new Scene(FXMLLoader.load(getClass().getResource("scenes/EditAppointment.fxml")), 900,600);
        customer_scene = new Scene(FXMLLoader.load(getClass().getResource("scenes/EditCustomer.fxml")), 900,600);

        primaryStage.setTitle(DefaultLocale.translate("title"));
        primaryStage.setScene(login_scene);
        MenuController.setLocation(login_scene);
        primaryStage.show();
    }

    /**
     * Entry point to the application.
     * @param args Passed to the application.launch command.
     */
    public static void main(String[] args) {
        DefaultLocale.init();
        JDBC.makeConnection();
        launch(args);
    }

    /**
     * <p>Changes the window's scene.</p>
     * <p>Copies window width and height to the new scene for a consistent experience.</p>
     * @param scene The new scene.
     */
    public static void setScene(Scene scene) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        MenuController.setLocation(scene);
        stage.setScene(scene);
        stage.setHeight(height);
        stage.setWidth(width);
    }

    /**
     * Gets the current scene.
     * @return The current scene.
     */
    public static Scene getScene(){
        return stage.getScene();
    }
}
