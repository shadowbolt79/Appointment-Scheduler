package scheduler.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import scheduler.DefaultLocale;
import scheduler.records.User;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The Login Controller controls both the Login Scene and functions related to logging in.
 * @author Ken Butler
 */
@SuppressWarnings({"unused", "ResultOfMethodCallIgnored"})
public class LoginController {
    /**
     * Instanced LoginController so controller can be accessed from other scene controllers or from static reference.
     */
    public static LoginController INSTANCE;

    //FXML fields filled in by JavaFX loader.
    @FXML private Label l_login_title, l_login_username, l_login_password, l_login_error, l_login_location;
    @FXML private TextField tf_login_username;
    @FXML private PasswordField pf_login_password;
    @FXML private Button b_login;

    @FXML private URL location;
    @FXML private ResourceBundle resources;
    @FXML private MenuBar menu_bar;

    /**
     * Default constructor.
     */
    public LoginController() {}

    /**
     * Called by JavaFX to do initial setup of the Login Scene.
     *
     * Translates all strings into either English or French, then connects the logic so it can attempt to
     * log in upon user interaction.
     */
    @FXML private void initialize() {
        INSTANCE=this;

        l_login_title.setText(DefaultLocale.translate(l_login_title.getText()));
        l_login_username.setText(DefaultLocale.translate(l_login_username.getText()));
        l_login_password.setText(DefaultLocale.translate(l_login_password.getText()));
        b_login.setText(DefaultLocale.translate(b_login.getText()));

        pf_login_password.setText("");
        l_login_location.setText(
                DefaultLocale.translate("location") + ":" + DefaultLocale.country + " (" +
                DefaultLocale.zone.getDisplayName(TextStyle.SHORT, Locale.getDefault()) + ")"
        );

        tf_login_username.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
                attemptLogin();
        });

        pf_login_password.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER)
                attemptLogin();
        });

        b_login.setOnAction(event -> attemptLogin());
    }

    /**
     * Attempts to log in the user by taking the username and password, and comparing results to the database, and
     * logs the results to login_activity.txt.
     *
     * Displays an error if it was unsuccessful. Records the user and takes them to the calendar scene if successful.
     */
    void attemptLogin() {
        try {
            File file = new File("login_activity.txt");
            if(!file.exists())
                file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file,true);
            fos.write(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("[MM/dd/yyyy - HH:mm:ss]: ")).getBytes(StandardCharsets.UTF_8));

            if (tf_login_username.getText().length() < 1) {
                l_login_error.setText(DefaultLocale.translate("need_username"));
                tf_login_username.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
                l_login_error.setVisible(true);

                fos.write(DefaultLocale.translate("login_failed_username").getBytes());
            } else if (pf_login_password.getText().length() < 1) {
                l_login_error.setText(DefaultLocale.translate("need_password"));
                tf_login_username.setStyle("");
                pf_login_password.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
                l_login_error.setVisible(true);

                fos.write((tf_login_username.getText() + DefaultLocale.translate("login_failed_password")).getBytes());
            } else if (!User.attemptLogin(tf_login_username.getText(), pf_login_password.getText())) {
                l_login_error.setText(DefaultLocale.translate("incorrect_login"));
                tf_login_username.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
                pf_login_password.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
                l_login_error.setVisible(true);

                fos.write((tf_login_username.getText() + DefaultLocale.translate("login_failed_incorrect")).getBytes());
            } else {
                pf_login_password.setText("");
                tf_login_username.setStyle("");
                pf_login_password.setStyle("");
                l_login_error.setVisible(false);

                fos.write((tf_login_username.getText() + DefaultLocale.translate("login_succeeded")).getBytes());
            }

            fos.close();
        } catch (IOException ignored) {}
    }
}
