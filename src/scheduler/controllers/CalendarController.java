package scheduler.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import scheduler.*;
import scheduler.elements.*;
import scheduler.records.Appointment;
import scheduler.records.Contact;
import scheduler.records.Customer;
import scheduler.records.User;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * <p>The controller for the Calendar Scene.</p>
 * <p>Sets up translations and provides functionality for all 3 tabs of the user interface: Week, Calendar, and List view.</p>
 * <p>Week View shows a visual time table during the company's working hours of the user's appointments.</p>
 * <p>Calendar shows a visual Calendar for an entire month's view.</p>
 * <p>List View can be sorted into either month or week and is a simple list of appointments.</p>
 * @author Ken Butler
 */
@SuppressWarnings("unused")
public class CalendarController implements User.UserListener {

    /**
     * Instanced CalendarController to be referenced from other Scene Controllers.
     */
    public static CalendarController INSTANCE;

    //FXML fields filled by JavaFX.
    @FXML private Label l_sunday_date, l_month_year, l_appointments, l_list_date, l_month_or_week;
    @FXML private Tab tpt_week_view, tpt_calendar_view, tpt_list_view;
    @FXML private TabPane tp_view;
    @FXML private TableView<CalendarWeek> tv_week_view, tv_calendar_view;
    @FXML private TableView<Appointment> tv_list_view;
    @FXML private Button b_prev_week, b_prev_month, b_prev_month_cal, b_prev_year, b_next_week, b_next_month,
            b_next_month_cal, b_next_year, b_new_appointment, b_edit_appointment, b_cancel_appointment,
            b_prev_week_list, b_prev_month_list, b_next_week_list, b_next_month_list;
    @FXML private RadioButton rb_week, rb_month;
    @FXML private AnchorPane ap_appointments;

    @FXML private TableColumn<CalendarWeek, CalendarDay> tc_w_sunday,tc_w_monday,tc_w_tuesday,tc_w_wednesday,
            tc_w_thursday,tc_w_friday,tc_w_saturday,tc_c_sunday,tc_c_monday,tc_c_tuesday,tc_c_wednesday,tc_c_thursday,
            tc_c_friday,tc_c_saturday;

    @FXML private TableColumn<Appointment, Integer> tc_l_id;
    @FXML private TableColumn<Appointment, String> tc_l_title, tc_l_description, tc_l_location, tc_l_type, tc_l_start_date_time, tc_l_end_date_time;
    @FXML private TableColumn<Appointment, Contact> tc_l_contact;
    @FXML private TableColumn<Appointment, Customer> tc_l_customer_id;
    @FXML private TableColumn<Appointment, User> tc_l_user_id;

    @FXML private AnchorPane side_pane;
    @FXML private MenuBar menu_bar;

    /**
     * Represents the day the user has selected within the calendar. Is used for auto supplying dates for new
     * appointments, as well as can be listened to for graphical updates in table cells.
     */
    public static final SimpleObjectProperty<ZonedDateTime> selectedDay = new SimpleObjectProperty<>();

    /**
     * Represents the selected appointment. Can be listened to for graphical updates in the table cells.
     */
    public static final SimpleObjectProperty<Appointment> selectedAppointment = new SimpleObjectProperty<>();

    /**
     * Used to prevent cascading/recursive updates.
     */
    private boolean isUpdating=false;

    /**
     * Empty constructor.
     */
    public CalendarController(){}

    /**
     * <p>Initializes the Calendar Scene.</p>
     * <p>Translates strings, binds element locations in case of screen resizing, and handles logic button presses.
     * All button events use lambda for organization sake.</p>
     * <p>Buttons that cause the scene to change to another, which is exclusively the Edit Appointment Scene, lambda
     * is used to process the return from the Edit Appointment Scene and reselect the appointment, if one was
     * provided by the scene.</p>
     */
    @FXML
    private void initialize(){
        INSTANCE=this;

        //Variable initialization
        User.registerListener(this);
        selectedDay.set(ZonedDateTime.now());
        tp_view.getSelectionModel().select(tpt_calendar_view);
        tv_week_view.setSelectionModel(null);
        tv_calendar_view.setSelectionModel(null);
        l_appointments.setWrapText(true);
        l_appointments.setText("");

        //Bind variables so they stay relevant during window resizing
        l_sunday_date.translateXProperty().bind(tv_week_view.widthProperty().multiply(0.5d).subtract(100));
        l_month_year.translateXProperty().bind(tv_calendar_view.widthProperty().multiply(0.5d).subtract(100));
        b_prev_week.translateXProperty().bind(tv_week_view.widthProperty().multiply(0.5d).subtract(142));
        b_prev_month.translateXProperty().bind(tv_week_view.widthProperty().multiply(0.5d).subtract(197));
        b_next_week.translateXProperty().bind(tv_week_view.widthProperty().multiply(0.5d).add(100));
        b_next_month.translateXProperty().bind(tv_week_view.widthProperty().multiply(0.5d).add(155));
        b_prev_month_cal.translateXProperty().bind(tv_calendar_view.widthProperty().multiply(0.5d).subtract(142));
        b_prev_year.translateXProperty().bind(tv_calendar_view.widthProperty().multiply(0.5d).subtract(197));
        b_next_month_cal.translateXProperty().bind(tv_calendar_view.widthProperty().multiply(0.5d).add(100));
        b_next_year.translateXProperty().bind(tv_calendar_view.widthProperty().multiply(0.5d).add(155));

        b_new_appointment.translateXProperty().bind(ap_appointments.widthProperty().subtract(b_new_appointment.widthProperty()).subtract(10));
        b_new_appointment.translateYProperty().bind(ap_appointments.heightProperty().subtract(35));
        b_edit_appointment.translateXProperty().bind(b_new_appointment.translateXProperty().subtract(10).subtract(b_edit_appointment.widthProperty()));
        b_edit_appointment.translateYProperty().bind(ap_appointments.heightProperty().subtract(35));
        b_cancel_appointment.translateXProperty().bind(b_edit_appointment.translateXProperty().subtract(10).subtract(b_cancel_appointment.widthProperty()));
        b_cancel_appointment.translateYProperty().bind(ap_appointments.heightProperty().subtract(35));

        l_appointments.maxWidthProperty().bind(ap_appointments.widthProperty().subtract(20));

        //Translate fields
        tpt_week_view.setText(DefaultLocale.translate("week"));
        tpt_calendar_view.setText(DefaultLocale.translate("calendar"));

        b_new_appointment.setText(DefaultLocale.translate("new"));
        b_edit_appointment.setText(DefaultLocale.translate("edit"));
        b_cancel_appointment.setText(DefaultLocale.translate("delete"));

        //Bind and translate table columns for Calendar and Week Views.
        ArrayList<TableColumn<CalendarWeek, CalendarDay>> days = new ArrayList<>();
        days.add(tc_w_sunday);
        days.add(tc_w_monday);
        days.add(tc_w_tuesday);
        days.add(tc_w_wednesday);
        days.add(tc_w_thursday);
        days.add(tc_w_friday);
        days.add(tc_w_saturday);
        days.add(tc_c_sunday);
        days.add(tc_c_monday);
        days.add(tc_c_tuesday);
        days.add(tc_c_wednesday);
        days.add(tc_c_thursday);
        days.add(tc_c_friday);
        days.add(tc_c_saturday);
        for(int i = 0; i<days.size();i++) {
            TableColumn<CalendarWeek, CalendarDay> day = days.get(i);

            TableView<CalendarWeek> view = (i<7)?tv_week_view:tv_calendar_view;
            day.setCellFactory((i<7)?calendarWeekCalendarDayTableColumn -> new WeekdayCell(view): calendarWeekCalendarDayTableColumn -> new CalendarDayCell(view));

            day.prefWidthProperty().bind(view.widthProperty().multiply(1d/7d));
            switch(i%7) {
                case 0 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("sunday"));
                    day.setText(DefaultLocale.translate("sunday"));
                }
                case 1 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("monday"));
                    day.setText(DefaultLocale.translate("monday"));
                }
                case 2 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("tuesday"));
                    day.setText(DefaultLocale.translate("tuesday"));
                }
                case 3 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("wednesday"));
                    day.setText(DefaultLocale.translate("wednesday"));
                }
                case 4 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("thursday"));
                    day.setText(DefaultLocale.translate("thursday"));
                }
                case 5 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("friday"));
                    day.setText(DefaultLocale.translate("friday"));
                }
                case 6 -> {
                    day.setCellValueFactory(new PropertyValueFactory<>("saturday"));
                    day.setText(DefaultLocale.translate("saturday"));
                }
            }
        }

        //Set Action Events
        b_prev_week.setOnAction(event -> setDay(selectedDay.get().minusWeeks(1)));
        b_prev_month.setOnAction(event -> setDay(selectedDay.get().minusMonths(1)));
        b_prev_month_cal.setOnAction(event -> setDay(selectedDay.get().minusMonths(1)));
        b_prev_year.setOnAction(event -> setDay(selectedDay.get().minusYears(1)));
        b_next_week.setOnAction(event -> setDay(selectedDay.get().plusWeeks(1)));
        b_next_month.setOnAction(event -> setDay(selectedDay.get().plusMonths(1)));
        b_next_month_cal.setOnAction(event -> setDay(selectedDay.get().plusMonths(1)));
        b_next_year.setOnAction(event -> setDay(selectedDay.get().plusYears(1)));

        //Lambda used to process what happens on return from the scene they go to.
        b_new_appointment.setOnAction(event -> {
            EditAppointmentController.INSTANCE.setAppointment(null);
            EditAppointmentController.INSTANCE.setDate(selectedDay.get());
            EditAppointmentController.INSTANCE.setFrom(Main.calender_scene,appointment -> {
                refresh_appointments();
                if(appointment!=null)
                    setSelectedAppointment(appointment);
            });
            Main.setScene(Main.appointment_scene);
        });
        b_edit_appointment.setOnAction(event -> {
            if(selectedAppointment.get()!=null) {
                EditAppointmentController.INSTANCE.setAppointment(selectedAppointment.get());
                EditAppointmentController.INSTANCE.setFrom(Main.calender_scene,appointment -> {
                    refresh_appointments();
                    if(appointment!=null)
                        setSelectedAppointment(appointment);
                });
                Main.setScene(Main.appointment_scene);
            }
        });
        b_cancel_appointment.setOnAction(actionEvent -> {
            if(selectedAppointment.get()!=null){
                AlertBox.show(DefaultLocale.translate("warning"),DefaultLocale.translate("cancel_confirmation").replace("%1%",Integer.toString(selectedAppointment.get().appointmentID())).replace("%2%", selectedAppointment.get().type()).replace("%3%",selectedAppointment.get().customer().name()),()->{
                    if(selectedAppointment.get().cancelAppointment()){
                        removeAppointment(selectedAppointment.get());
                        selectedAppointment.set(null);
                    }
                });
            }
        });

        //Selected Appointment
        b_edit_appointment.visibleProperty().bind(selectedAppointment.isNotNull());
        b_cancel_appointment.visibleProperty().bind(selectedAppointment.isNotNull());

        selectedAppointment.addListener(event ->{
            if(selectedAppointment.get()!=null) {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
                DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("(MM/dd/yyyy)");
                l_appointments.setText(DefaultLocale.translate("appointment_details")+
                        "\n\n"+DefaultLocale.translate("id")+": "+selectedAppointment.get().appointmentID()+
                        "\n" +selectedAppointment.get().title() + " - " + selectedAppointment.get().type() + " " + selectedAppointment.get().start().format(dtf2) +
                        "\n(" +selectedAppointment.get().start().format(dtf) + " - " + selectedAppointment.get().end().format(dtf) + ")\n" +

                        "\n" + DefaultLocale.translate("location") + ": " + selectedAppointment.get().location() +
                        "\n" + DefaultLocale.translate("user") + ": " + selectedAppointment.get().user() +
                        "\n" + DefaultLocale.translate("customer") + ": " + selectedAppointment.get().customer() +
                        "\n" + DefaultLocale.translate("contact") + ": " + selectedAppointment.get().contact() +
                        "\n\n" + DefaultLocale.translate("description") + ": " + selectedAppointment.get().description()
                );
                if (selectedAppointment.get().start().getDayOfYear() != selectedDay.get().getDayOfYear())
                    setDay(selectedAppointment.get().start());
                else {
                    refresh_views();
                }
            }
            else {
                l_appointments.setText("");
                refresh_views();
            }

            isUpdating=true;
            if(tv_list_view.getItems().contains(selectedAppointment.get()))
                tv_list_view.getSelectionModel().select(selectedAppointment.get());
            else
                tv_list_view.getSelectionModel().clearSelection();
            isUpdating=false;
        });


        //List View begins here.
        tpt_list_view.setText(DefaultLocale.translate("list"));
        l_month_or_week.setText(DefaultLocale.translate("month"));

        l_list_date.translateXProperty().bind(tv_list_view.widthProperty().multiply(0.5d).subtract(100));
        b_prev_week_list.translateXProperty().bind(tv_list_view.widthProperty().multiply(0.5d).subtract(142));
        b_prev_month_list.translateXProperty().bind(tv_list_view.widthProperty().multiply(0.5d).subtract(197));
        b_next_week_list.translateXProperty().bind(tv_list_view.widthProperty().multiply(0.5d).add(100));
        b_next_month_list.translateXProperty().bind(tv_list_view.widthProperty().multiply(0.5d).add(155));

        rb_month.translateXProperty().bind(tv_list_view.widthProperty().subtract(10).subtract(rb_month.widthProperty()));
        rb_week.translateXProperty().bind(rb_month.translateXProperty().subtract(10).subtract(rb_week.widthProperty()));
        l_month_or_week.translateXProperty().bind(rb_month.translateXProperty().subtract(5).subtract(l_month_or_week.widthProperty().divide(2)));
        l_list_date.textProperty().bind(l_month_year.textProperty());

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM hh:mm a");

        tc_l_id.setCellValueFactory(appointmentIntegerCellDataFeatures -> new SimpleIntegerProperty(appointmentIntegerCellDataFeatures.getValue().appointmentID()).asObject());
        tc_l_title.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().title()));
        tc_l_description.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().description()));
        tc_l_location.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().location()));
        tc_l_contact.setCellValueFactory(appointmentContactCellDataFeatures -> new SimpleObjectProperty<>(appointmentContactCellDataFeatures.getValue().contact()));
        tc_l_type.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().type()));
        tc_l_start_date_time.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().start().format(dtf)));
        tc_l_end_date_time.setCellValueFactory(appointmentStringCellDataFeatures -> new SimpleStringProperty(appointmentStringCellDataFeatures.getValue().end().format(dtf)));
        tc_l_customer_id.setCellValueFactory(appointmentCustomerCellDataFeatures -> new SimpleObjectProperty<>(appointmentCustomerCellDataFeatures.getValue().customer()));
        tc_l_user_id.setCellValueFactory(appointmentUserCellDataFeatures -> new SimpleObjectProperty<>(appointmentUserCellDataFeatures.getValue().user()));

        tc_l_id.setText(DefaultLocale.translate("id"));
        tc_l_title.setText(DefaultLocale.translate("title"));
        tc_l_description.setText(DefaultLocale.translate("description"));
        tc_l_location.setText(DefaultLocale.translate("location"));
        tc_l_contact.setText(DefaultLocale.translate("contact"));
        tc_l_type.setText(DefaultLocale.translate("type"));
        tc_l_start_date_time.setText(DefaultLocale.translate("start_time"));
        tc_l_end_date_time.setText(DefaultLocale.translate("end_time"));
        tc_l_customer_id.setText(DefaultLocale.translate("customer"));
        tc_l_user_id.setText(DefaultLocale.translate("user"));

        tv_list_view.getSelectionModel().selectedItemProperty().addListener((obs,old, selection)->{
            if(selection==null||isUpdating)
                return;

            setSelectedAppointment(selection);
        });


        rb_week.setOnAction(event -> {
            l_month_or_week.setText(DefaultLocale.translate(rb_week.isSelected()?"week":"month"));
            refresh_appointments();
            l_list_date.textProperty().bind(rb_week.isSelected()?l_sunday_date.textProperty():l_month_year.textProperty());
        });
        rb_month.setOnAction(rb_week.getOnAction());

        b_prev_month_list.setOnAction(actionEvent -> setDay(rb_week.isSelected()?selectedDay.get().minusMonths(1):selectedDay.get().minusYears(1)));
        b_prev_week_list.setOnAction(actionEvent -> setDay(rb_week.isSelected()?selectedDay.get().minusWeeks(1):selectedDay.get().minusMonths(1)));
        b_next_week_list.setOnAction(actionEvent -> setDay(rb_week.isSelected()?selectedDay.get().plusWeeks(1):selectedDay.get().plusMonths(1)));
        b_next_month_list.setOnAction(actionEvent -> setDay(rb_week.isSelected()?selectedDay.get().plusMonths(1):selectedDay.get().plusYears(1)));
    }

    /////////////////////////////////
    //    Appointment Functions    //
    /////////////////////////////////

    /**
     * <p>Adds a single appointment to the calendar.</p>
     * <p>Works for both the calendar view and week view.</p>
     * @param appointment The appointment to be added.
     */
    public void addAppointment(Appointment appointment){
        if(appointment==null)return;
        for(CalendarWeek week:tv_calendar_view.getItems()){
            //Shift java's mon-sun week schedule to a sun-sat schedule
            //+1 days -> 7th day of the week -> -1 week
            if(appointment.start().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1).getDayOfYear() == week.getSunday().date().getDayOfYear()){
                switch (appointment.start().getDayOfWeek()) {
                    case SUNDAY -> week.getSunday().appointments().add(appointment);
                    case MONDAY -> week.getMonday().appointments().add(appointment);
                    case TUESDAY -> week.getTuesday().appointments().add(appointment);
                    case WEDNESDAY -> week.getWednesday().appointments().add(appointment);
                    case THURSDAY -> week.getThursday().appointments().add(appointment);
                    case FRIDAY -> week.getFriday().appointments().add(appointment);
                    case SATURDAY -> week.getSaturday().appointments().add(appointment);
                }
                break;
            }
        }
        refresh_views();
    }

    /**
     * <p>Removes a single appointment from the calendar.</p>
     * <p>Works for both the Calendar and Week Views.</p>
     * @param original The appointment to be removed.
     */
    public void removeAppointment(Appointment original){
        if(selectedAppointment.get().equals(original)){
            selectedAppointment.set(null);
        }
        for(CalendarWeek week:tv_calendar_view.getItems()){
            //Shift java's mon-sun week schedule to a sun-sat schedule
            //+1 days -> 7th day of the week -> -1 week
            if (original.start().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1).getDayOfYear() == week.getSunday().date().getDayOfYear()) {
                week.getSunday().appointments().remove(original);
                week.getMonday().appointments().remove(original);
                week.getTuesday().appointments().remove(original);
                week.getWednesday().appointments().remove(original);
                week.getThursday().appointments().remove(original);
                week.getFriday().appointments().remove(original);
                week.getSaturday().appointments().remove(original);
                break;
            }
        }
        refresh_views();
    }

    /**
     * <p>Replaces one appointment with another.</p>
     * <p>Works for both Calendar and Week Views. Operates in one sweep.</p>
     * @param original The appointment to replace
     * @param replacement The new appointment
     */
    public void updateAppointment(Appointment original, Appointment replacement){
        boolean found1=false, found2=false;
        for(CalendarWeek week:tv_calendar_view.getItems()){
            if (original.start().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1).getDayOfYear() == week.getSunday().date().getDayOfYear()) {
                week.getSunday().appointments().remove(original);
                week.getMonday().appointments().remove(original);
                week.getTuesday().appointments().remove(original);
                week.getWednesday().appointments().remove(original);
                week.getThursday().appointments().remove(original);
                week.getFriday().appointments().remove(original);
                week.getSaturday().appointments().remove(original);
                found1=true;
            }
            if(replacement.start().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1).getDayOfYear() == week.getSunday().date().getDayOfYear()){
                switch (replacement.start().getDayOfWeek()) {
                    case SUNDAY -> week.getSunday().appointments().add(replacement);
                    case MONDAY -> week.getMonday().appointments().add(replacement);
                    case TUESDAY -> week.getTuesday().appointments().add(replacement);
                    case WEDNESDAY -> week.getWednesday().appointments().add(replacement);
                    case THURSDAY -> week.getThursday().appointments().add(replacement);
                    case FRIDAY -> week.getFriday().appointments().add(replacement);
                    case SATURDAY -> week.getSaturday().appointments().add(replacement);
                }
                found2=true;
            }
            if(found1&&found2)break;
        }
        refresh_views();
    }

    /**
     * <p>Refreshes the Views with fresh copies from the database.</p>
     * <p>Used primarily when first logged in. Also used if the current user is admin, and they cycle through different users.</p>
     */
    public void refresh_appointments() {
        //Calendar and Week View Updates
        if (!tv_calendar_view.getItems().isEmpty()) {
            for (CalendarWeek week : tv_calendar_view.getItems()) {
                week.getSunday().appointments().clear();
                week.getMonday().appointments().clear();
                week.getTuesday().appointments().clear();
                week.getWednesday().appointments().clear();
                week.getThursday().appointments().clear();
                week.getFriday().appointments().clear();
                week.getSaturday().appointments().clear();
            }
            if (User.current != null) {
                ZonedDateTime first = selectedDay.get().with(ChronoField.DAY_OF_MONTH, 2)
                        .with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1)
                        .with(ChronoField.SECOND_OF_DAY, 0).truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime last = selectedDay.get().with(ChronoField.DAY_OF_MONTH, 2).plusMonths(1)
                        .with(ChronoField.DAY_OF_WEEK, 7).truncatedTo(ChronoUnit.DAYS);
                ArrayList<Appointment> appointments = Appointment.getAppointments(SideMenuController.INSTANCE.getCurrentUser(), first, last);
                if (appointments != null) {
                    int i = 0;
                    boolean found;
                    for (Appointment appointment : appointments) {
                        found = false;
                        while (!found) {
                            if (i >= tv_calendar_view.getItems().size())
                                break;
                            CalendarWeek week = tv_calendar_view.getItems().get(i);
                            if (appointment.start().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1)
                                    .getDayOfYear() == week.getSunday().date().getDayOfYear()) {
                                found = true;
                                switch (appointment.start().getDayOfWeek()) {
                                    case SUNDAY -> week.getSunday().appointments().add(appointment);
                                    case MONDAY -> week.getMonday().appointments().add(appointment);
                                    case TUESDAY -> week.getTuesday().appointments().add(appointment);
                                    case WEDNESDAY -> week.getWednesday().appointments().add(appointment);
                                    case THURSDAY -> week.getThursday().appointments().add(appointment);
                                    case FRIDAY -> week.getFriday().appointments().add(appointment);
                                    case SATURDAY -> week.getSaturday().appointments().add(appointment);
                                }
                            } else i++;
                        }
                        if (i >= tv_calendar_view.getItems().size())
                            break;

                    }
                }
            }
        }

        refresh_views();
    }

    /**
     * <p>Causes graphical updates in the side menu and updates the List View with the latest set of appointments.</p>
     * <p>Called whenever one of the appointments is changed, or when refresh_appointments() is called.</p>
     */
    public void refresh_views(){
        if(SideMenuController.INSTANCE.getCurrentUser()==null)
            return;

        SideMenuController.refreshAppointments();
        //List view updates
        isUpdating = true;

        ZonedDateTime start, end;
        if (rb_week.isSelected()) {
            start = selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK, 7).minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            end = start.plusWeeks(1);
        } else {
            start = selectedDay.get().with(ChronoField.DAY_OF_MONTH, 1).truncatedTo(ChronoUnit.DAYS);
            end = start.plusMonths(1);
        }
        Appointment a = tv_list_view.getSelectionModel().getSelectedItem();
        ArrayList<Appointment> list = Appointment.getAppointments(SideMenuController.INSTANCE.getCurrentUser(), start, end);
        tv_list_view.getItems().clear();
        if (list != null)
            tv_list_view.getItems().addAll(list);

        if (a != null && tv_list_view.getItems().contains(a))
            tv_list_view.getSelectionModel().select(a);
        else
            tv_list_view.getSelectionModel().clearSelection();

        //Resize the columns based on contents
        tv_list_view.getColumns().forEach((column) -> {
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < tv_list_view.getItems().size(); i++) {
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double width = t.getLayoutBounds().getWidth();
                    if (width > max) {
                        max = width;
                    }
                }
            }
            column.setPrefWidth(max + 10.0d);
        });

        isUpdating = false;
    }

    ////////////////
    //    Sets    //
    ////////////////

    /**
     * <p>Sets the selected day to a new day.</p>
     * <p>Selected day is used for tracking what time range the different Views show, and fills in the date for the
     * New Appointment Scene.</p>
     * <p>Changing the selected day will also cause graphical updates to CalendarDay cells listening to it.</p>
     * @param day A brand new day.
     */
    public void setDay(ZonedDateTime day) {
        if(day.getDayOfYear()!=selectedDay.get().getDayOfYear()||day.getYear()!=selectedDay.get().getYear()) {
            selectedDay.set(day);
            if(selectedAppointment.get()!=null) {
                if (day.getDayOfYear() != selectedAppointment.get().start().getDayOfYear()||day.getYear()!=selectedAppointment.get().start().getYear()) {
                    setSelectedAppointment(null);
                    l_appointments.setText("");
                }
            }
            onDayChange();
        }
    }

    /**
     * <p>Sets the selected appointment.</p>
     * <p>The selected appointment will populate text details on the right hand side of the Calendar Scene, as well as
     * enable the New Appointment Scene to edit an existing appointment.</p>
     * @param appointment The selected appointment.
     */
    public void setSelectedAppointment(Appointment appointment) {
        if(selectedAppointment.get()!=appointment) {
            selectedAppointment.set(appointment);
        }
    }

    //////////////////
    //    Events    //
    //////////////////

    /**
     * <p>Runs when the calendar day changes, and might need major updates.</p>
     * <p>Updates the displayed text for month and year views. Also reinitializes the calendar when the date goes outside
     * the range for the loaded month.</p>
     */
    public void onDayChange() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        l_sunday_date.setText(selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,7).plusWeeks(-1).format(dtf) +
                " - " + selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,6).format(dtf));
        l_month_year.setText(DefaultLocale.translate(selectedDay.get().getMonth().toString().toLowerCase())
                +", "+selectedDay.get().getYear());

        //If the calendar is not initialized, or is out of range, initialize the calendar
        if(tv_calendar_view.getItems().isEmpty()||
                tv_calendar_view.getItems().get(0).getSunday().date().getDayOfYear()!=
                        selectedDay.get().with(ChronoField.DAY_OF_MONTH,1).plusDays(1).with(ChronoField.DAY_OF_WEEK,7)
                                .minusWeeks(1).getDayOfYear()) {
            tv_calendar_view.getItems().clear();
            tv_week_view.getItems().clear();
            ZonedDateTime first = selectedDay.get().with(ChronoField.DAY_OF_MONTH,1).plusDays(1)
                    .with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1).with(ChronoField.SECOND_OF_DAY,0);
            int wrongMonth = selectedDay.get().getMonthValue()+1;
            wrongMonth=wrongMonth>12?1:wrongMonth;
            do
            {
                CalendarWeek week = new CalendarWeek(first);
                tv_calendar_view.getItems().add(week);
                if(week.getSunday().date().getDayOfYear()==selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1).getDayOfYear())
                    tv_week_view.getItems().add(week);
                first=first.plusWeeks(1);
            }
            while(first.getMonthValue()!=wrongMonth);
            refresh_appointments();
        }
        else {
            if(tv_week_view.getItems().get(0).getSunday().date().getDayOfYear()!=
                selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1).getDayOfYear()) {
                tv_week_view.getItems().clear();
                for (CalendarWeek week : tv_calendar_view.getItems()) {
                    if (week.getSunday().date().getDayOfYear() == selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,
                            7).minusWeeks(1).getDayOfYear()) {
                        tv_week_view.getItems().add(week);
                    }
                }
            }
            refresh_views();
        }
    }

    /**
     * <p>Called when a user logs in or out.</p>
     * <p>Also called for admin when they change who they're viewing appointments of.</p>
     */
    @Override
    public void onUserChange() {
        onDayChange();
        refresh_appointments();
    }
}
