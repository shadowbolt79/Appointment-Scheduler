package scheduler.elements;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import scheduler.DefaultLocale;

/**
 * A simple wrapper for JavaFX Alert.
 *
 * Allows for the creation and display of popup windows with the single {@link #show(String, String, Runnable)} command.
 */
public class AlertBox {
    private static Alert alert;
    private static ButtonType okButtonType;

    /**
     * Initializes the alert box with yes/no buttons.
     */
    private static void init(){
        okButtonType = new ButtonType(DefaultLocale.translate("yes_caps"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(DefaultLocale.translate("no_caps"), ButtonBar.ButtonData.CANCEL_CLOSE);
        alert = new Alert(Alert.AlertType.CONFIRMATION,"TEST",okButtonType, cancelButtonType);
    }

    /**
     * <p>Displays an alert with a custom title and message.</p>
     * <p>Callback is called only if user clicks 'Yes'. Lambda is used when this function is called to inline code for organizational sake.</p>
     * <p>Closes the previous dialog if it was still open.</p>
     * @param title Title of the dialog
     * @param text Message text of the dialog
     * @param callback Function that is run if the user clicks yes. Usually provided via lambda to keep the code together.
     */
    public static void show(String title, String text, Runnable callback){
        if(alert==null)
            init();
        if(alert.isShowing())
            alert.close();
        alert.setHeaderText(title);
        alert.setContentText(text);
        alert.showAndWait().filter(response -> response == okButtonType).ifPresent(response -> {
            alert.hide();
            callback.run();
        });
    }
}
