package scheduler.controllers;

import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import scheduler.DefaultLocale;
import scheduler.Main;
import scheduler.interfaces.BackTracker;
import scheduler.records.Country;
import scheduler.records.Customer;
import scheduler.records.Divisions;

/**
 * <p>Controller for the Edit/New Customer Scene.</p>
 * <p>Translates and provides functionality for the scene. Button's functions are provided with lambda for organization's
 * sake.</p>
 * @author Ken Butler
 */
@SuppressWarnings("unused")
public class EditCustomerController extends BackTracker<Customer> {
    /**
     * Static Instance of the controller for referencing in other controllers.
     */
    public static EditCustomerController INSTANCE;

    //FXML fields are initialized by JavaFX.
    @FXML private Button b_back, b_save, b_ok, b_cancel, b_delete;
    @FXML private TextField tf_name, tf_phone, tf_address, tf_location, tf_id;
    @FXML private Label l_name, l_edit_customer, l_postal_code, l_address, l_phone, l_errors, l_country, l_first_division, l_id;
    @FXML private ChoiceBox<Country> cb_country;
    @FXML private ChoiceBox<Divisions> cb_first_division;
    @FXML private AnchorPane ap_container;

    @FXML private TableView<Customer> tv_customers;
    @FXML private TableColumn<Customer, Integer> tc_id;
    @FXML private TableColumn<Customer, String> tc_name, tc_phone, tc_address, tc_postal_code;
    @FXML private TableColumn<Customer, Divisions> tc_first_division;
    @FXML private TableColumn<Customer, Country> tc_country;

    @FXML private AnchorPane side_pane;
    @FXML private MenuBar menu_bar;

    //Used to prevent recursive/cascading updates
    private boolean isUpdating = false;

    //If null, scene is creating a new customer. Otherwise, editing selected.
    private Customer selected = null;

    /**
     * Default constructor.
     */
    public EditCustomerController(){}

    /**
     * Called by JavaFX to initialize the scene.
     * <p><p>
     * Translates and provides functionality for the scene. Button's functions are provided with lambda for organization's
     * sake.
     */
    @FXML
    private void initialize() {
        INSTANCE = this;

        refreshCustomers();

        //Prevent user from typing too much
        tf_name.setOnKeyTyped(keyEvent -> {
            if(tf_name.getText().length()>=50) {
                int c = tf_name.getCaretPosition();
                if(c>0&&c<50)
                    tf_name.setText(tf_name.getText(0, c-1)+tf_name.getText(c--, 51));
                else
                    tf_name.setText(tf_name.getText(0, 50));
                tf_name.positionCaret(c);
            }
        });
        tf_address.setOnKeyTyped(keyEvent -> {
            if(tf_address.getText().length()>=100) {
                int c = tf_address.getCaretPosition();
                if(c>0&&c<100)
                    tf_address.setText(tf_address.getText(0, c-1)+tf_address.getText(c--, 101));
                else
                    tf_address.setText(tf_address.getText(0, 100));
                tf_address.positionCaret(c);
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
        tf_phone.setOnKeyTyped(keyEvent -> {
            if(tf_phone.getText().length()>=50) {
                int c = tf_phone.getCaretPosition();
                if(c>0&&c<50)
                    tf_phone.setText(tf_phone.getText(0, c-1)+tf_phone.getText(c--, 51));
                else
                    tf_phone.setText(tf_phone.getText(0, 50));
                tf_phone.positionCaret(c);
            }
        });


        tc_id.setCellValueFactory(customerIntegerCellDataFeatures -> {
            if(customerIntegerCellDataFeatures.getValue().id()<0)return null;
            return new SimpleIntegerProperty(customerIntegerCellDataFeatures.getValue().id()).asObject();
        });
        tc_name.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleStringProperty(customerIntegerCellDataFeatures.getValue().name()));
        tc_phone.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleStringProperty(customerIntegerCellDataFeatures.getValue().phone_number()));
        tc_address.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleStringProperty(customerIntegerCellDataFeatures.getValue().address()));
        tc_postal_code.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleStringProperty(customerIntegerCellDataFeatures.getValue().postal_code()));
        tc_first_division.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleObjectProperty<>(customerIntegerCellDataFeatures.getValue().division()));
        tc_country.setCellValueFactory(customerIntegerCellDataFeatures -> new SimpleObjectProperty<>(customerIntegerCellDataFeatures.getValue().division().country()));

        cb_country.getItems().addAll(Country.getCountries());
        cb_country.setOnAction(event ->{
            //Updates first division field on select.
            cb_first_division.getItems().clear();
            if(cb_country.getValue()!=null) {
                if(cb_country.getValue().name().equals("U.S"))
                    l_first_division.setText(DefaultLocale.translate("state")+":");
                else if(cb_country.getValue().name().equals("UK"))
                    l_first_division.setText(DefaultLocale.translate("sovereignty")+":");
                else
                    l_first_division.setText(DefaultLocale.translate("province")+":");

                cb_first_division.getItems().addAll(Divisions.getDivisions(cb_country.getValue()));
            }
            cb_first_division.setValue(null);
        });

        tv_customers.getSelectionModel().selectedItemProperty().addListener((obs,old, selection)->{
            //selected item property might change due to other updates, such as saving a new customer.
            //prevent running code that is unneeded.
            if(selection==null||isUpdating)
                return;

            l_errors.setText("");

            //Set fields for either adding a new customer, or updating an existing one.
            if(selection==Customer.addNew){
                tf_name.setText("");
                tf_phone.setText("");
                tf_address.setText("");
                tf_location.setText("");
                tf_id.setText("");
                cb_country.setValue(null);
                selected=null;
                b_delete.setVisible(false);
            }
            else{
                selected=selection;
                tf_id.setText(Integer.toString(selection.id()));
                tf_name.setText(selection.name());
                tf_address.setText(selection.address());
                tf_location.setText(selection.postal_code());
                tf_phone.setText(selection.phone_number());
                cb_country.setValue(selection.division().country());
                cb_first_division.setValue(selection.division());
                b_delete.setVisible(true);
            }
        });
        tv_customers.setColumnResizePolicy(policy -> true);

        b_delete.setOnAction(event->deleteSelected());
        b_save.setOnAction(actionEvent -> saveSelected());

        b_cancel.setOnAction(actionEvent -> goBack());
        b_back.setOnAction(actionEvent -> goBack());
        b_ok.setOnAction(actionEvent -> goBack(tv_customers.getSelectionModel().getSelectedItem()));

        //Bind positions for window resizing
        tv_customers.prefWidthProperty().bind(ap_container.widthProperty().subtract(tv_customers.getLayoutX()).subtract(10));
        tv_customers.prefHeightProperty().bind(ap_container.heightProperty().subtract(tv_customers.getLayoutY()).subtract(50));

        b_ok.translateXProperty().bind(ap_container.widthProperty().subtract(b_ok.widthProperty()).subtract(10));
        b_ok.translateYProperty().bind(ap_container.heightProperty().subtract(b_ok.heightProperty()).subtract(10));
        b_cancel.translateXProperty().bind(b_ok.translateXProperty().subtract(b_cancel.widthProperty()).subtract(10));
        b_cancel.translateYProperty().bind(b_ok.translateYProperty());

        b_delete.translateXProperty().bind(b_save.layoutXProperty().subtract(b_delete.widthProperty()).subtract(10));
        b_delete.translateYProperty().bind(b_save.layoutYProperty());

        //Translations
        b_back.setText(DefaultLocale.translate("back"));
        b_save.setText(DefaultLocale.translate("save"));
        b_ok.setText(DefaultLocale.translate("ok"));
        b_cancel.setText(DefaultLocale.translate("cancel"));
        b_delete.setText(DefaultLocale.translate("delete"));

        l_id.setText(DefaultLocale.translate("id")+":");
        l_name.setText(DefaultLocale.translate("name")+":");
        l_edit_customer.setText(DefaultLocale.translate("edit_customer_title"));
        l_postal_code.setText(DefaultLocale.translate("postal_code")+":");
        l_address.setText(DefaultLocale.translate("address")+":");
        l_phone.setText(DefaultLocale.translate("phone")+":");
        l_country.setText(DefaultLocale.translate("country")+":");
        l_first_division.setText(DefaultLocale.translate("province")+":");

        tc_id.setText(DefaultLocale.translate("id"));
        tc_name.setText(DefaultLocale.translate("name"));
        tc_phone.setText(DefaultLocale.translate("phone"));
        tc_address.setText(DefaultLocale.translate("address"));
        tc_postal_code.setText(DefaultLocale.translate("postal_code"));
        tc_first_division.setText(DefaultLocale.translate("division"));
        tc_country.setText(DefaultLocale.translate("country"));
    }

    /**
     * <p>Attempts to delete the selected customer.</p>
     * <p>Uses {@link Customer#delete(Runnable)} to do so, as that will ask the user if they are sure, as well as
     * delete any appointments revolved around the customer as well.</p>
     */
    public void deleteSelected(){
        if(selected!=null&&selected!=Customer.addNew){
            selected.delete(()->{
                if(backScene== Main.appointment_scene)
                    EditAppointmentController.INSTANCE.setAppointment(null);
                tv_customers.getItems().remove(selected);
                tv_customers.getSelectionModel().select(Customer.addNew);
            });
        }
    }

    /**
     * <p>Attempts to save the customer.</p>
     * <p>First verifies that fields are valid, and displays errors if they are not. New and Existing customers are saved
     * via {@link Customer#createNew(String, String, String, String, Divisions)} and
     * {@link Customer#update(String, String, String, String, Divisions)} respectively.</p>
     */
    public void saveSelected(){
        boolean error = false;
        String errors = "";
        if(tf_name.getText().length()<3){
            errors+=DefaultLocale.translate("name_empty")+"\n";
            tf_name.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
            error=true;
        }
        else
            tf_name.setStyle("");

        //Regex matches most valid phone number formats. Does not accept ()'s.
        if(!tf_phone.getText().matches("^((\\+)?\\d{1,3}[- .])?(\\d{3}[- .]?){2}\\d{4}$")){
            errors+=DefaultLocale.translate("phone_incorrect")+"\n";
            tf_phone.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
            error=true;
        }
        else
            tf_phone.setStyle("");
        if(cb_country.getValue()==null){
            errors+=DefaultLocale.translate("country_empty")+"\n";
            cb_country.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
            error=true;
        }
        else
            cb_country.setStyle("");
        if(cb_first_division.getValue()==null){
            errors+=DefaultLocale.translate("division_empty")+"\n";
            cb_first_division.setStyle("-fx-border-color: red; -fx-border-width: 1 1 1 1;");
            error=true;
        }
        else
            cb_first_division.setStyle("");
        if(tf_address.getText().length()<5){
            errors+=DefaultLocale.translate("address_empty")+"\n";
            tf_address.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
            error=true;
        }
        else
            tf_address.setStyle("");
        if(tf_location.getText().length()<3){
            errors+=DefaultLocale.translate("location_empty")+"\n";
            tf_location.setStyle("-fx-text-box-border: red; -fx-focus-color: red ;");
            error=true;
        }
        else
            tf_location.setStyle("");

        l_errors.setText(errors);

        if(error)return;

        if(selected==null) {
            Customer c = Customer.createNew(tf_name.getText(), tf_address.getText(), tf_location.getText(), tf_phone.getText(), cb_first_division.getValue());
            if (c != null) {
                tv_customers.getItems().add(c);
                tv_customers.getSelectionModel().select(c);
            }
        }
        else {
            Customer c = selected.update(tf_name.getText(), tf_address.getText(), tf_location.getText(), tf_phone.getText(), cb_first_division.getValue());
            if (c != null) {
                tv_customers.getItems().set(tv_customers.getItems().indexOf(selected), c);
                tv_customers.getSelectionModel().select(c);
            }
        }
    }

    /**
     * Refreshes the list of customers with the latest copy from the database.
     */
    public void refreshCustomers(){
        isUpdating=true;
        Customer c = tv_customers.getSelectionModel().getSelectedItem();
        tv_customers.getItems().clear();
        tv_customers.getItems().addAll(Customer.getCustomers());
        tv_customers.getItems().add(Customer.addNew);
        if(c==null)
            tv_customers.getSelectionModel().select(Customer.addNew);
        else
            tv_customers.getSelectionModel().select(c);
        isUpdating=false;
    }

    /**
     * <p>Sets the selected customer.</p>
     * <p>Can be called from outside of the controller to pass in an existing customer as default.</p>
     * @param customer The customer.
     */
    public void setCustomer(Customer customer){
        refreshCustomers();
        if(tv_customers.getItems().contains(customer))
            tv_customers.getSelectionModel().select(customer);
        else
            tv_customers.getSelectionModel().select(Customer.addNew);
    }
}
