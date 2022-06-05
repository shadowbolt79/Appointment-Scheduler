package scheduler.elements;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import scheduler.records.Appointment;
import scheduler.DefaultLocale;
import scheduler.controllers.CalendarController;

import javafx.beans.value.ChangeListener;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * <p>Handles graphical updates and mouse clicks for the table cell in Week View.</p>
 * <p>Clicking on the time slot for an appointment will select it to be viewed/edited/copied. Clicking anywhere else in the
 * cell will select that date. Graphically shows a timeline of the appointments via work hours translated from EST.</p>
 * @author Ken Butler
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class WeekdayCell extends TableCell<CalendarWeek,CalendarDay> implements ChangeListener {
    private final Label l_date;
    AnchorPane background;
    private static final Background defaultBackground = new Background(new BackgroundFill(Color.rgb(255,255,255),new CornerRadii(0), Insets.EMPTY));
    private static final Background selectedBackground = new Background(new BackgroundFill(Color.rgb(185,185,255),new CornerRadii(0), Insets.EMPTY));

    private final TableView<CalendarWeek> tv;
    private final ArrayList<Node> appointments = new ArrayList<>();

    SimpleListProperty<Appointment> observableList;

    /**
     * <p>Creates a new Week cell with a reference to the holding tableview.</p>
     * <p>Binds cell sizes to table view and adds hour markers.</p>
     * @param parent The containing tableview.
     */
    public WeekdayCell(TableView<CalendarWeek> parent) {
        tv=parent;
        background = new AnchorPane();
        l_date = new Label();
        l_date.relocate(5,5);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh a-------------");
        ZonedDateTime openingHour = ZonedDateTime.parse("2011-12-03 08:00:00 EST",DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss z")).withZoneSameInstant(DefaultLocale.zone);

        for(int i = 0; i<15;i++) {
            Label hour = new Label(openingHour.plusHours(i).format(dtf));
            hour.translateYProperty().bind(parent.heightProperty().subtract(70).multiply(1f/14f * i).add(25));
            hour.setStyle("-fx-font-size:8;");
            background.getChildren().add(hour);
        }

        background.prefHeightProperty().bind(parent.heightProperty().add(-25));
        background.getChildren().add(l_date);
        background.setVisible(true);
        background.setBackground(defaultBackground);

        prefHeightProperty().bind(parent.heightProperty().add(-25));
        this.setGraphic(background);
        setOnMouseClicked(mouseEvent -> {
            if(this.getItem()!=null) {
                CalendarController.INSTANCE.setDay(getItem().date());
            }
        });
    }

    /**
     * Sets the day the cell watches to update the time table.
     * @param calendarDay The day
     * @param b ignored
     */
    @Override
    protected void updateItem(CalendarDay calendarDay, boolean b) {
        super.updateItem(calendarDay, b);
        if(calendarDay!=null&&!b) {
            updateAppointmentList();
        }

        if(observableList!=null) {
            observableList.removeListener(this);
            observableList = null;
        }

        if(calendarDay!=null&&calendarDay.appointments()!=null) {
            observableList = calendarDay.appointments();
            observableList.addListener(this);

            CalendarController.selectedDay.addListener(this);

            if (calendarDay.date().getDayOfYear() == CalendarController.selectedDay.get().getDayOfYear())
                background.setBackground(selectedBackground);
            else
                background.setBackground(defaultBackground);

        }
        else {
            CalendarController.selectedDay.removeListener(this);
        }
    }

    /**
     * Clears the listeners. Used for cleanup.
     */
    private void clearListeners(){
        if(observableList!=null)
            observableList.removeListener(this);
        CalendarController.selectedDay.removeListener(this);
    }

    /**
     * Updates the graphics that represent the list of appointments.
     *
     * Called when the list is either set or updated.
     */
    public void updateAppointmentList() {
        if(getItem()!=null){
            l_date.setText(getItem().date().format(DateTimeFormatter.ofPattern("MM/dd")));

            //Clear the previous appointments and rebuild them
            background.getChildren().removeAll(appointments);
            appointments.clear();
            int i = 0;
            ZonedDateTime time = null;
            ZonedDateTime openingHour = ZonedDateTime.parse("2011-12-03 08:00:00 EST",DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss z")).withZoneSameInstant(DefaultLocale.zone);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm a");
            for(Appointment appointment:getItem().appointments())
            {
                Rectangle appointmentPane = new Rectangle();
                if(time==null) {
                    appointmentPane.widthProperty().bind(tv.widthProperty().divide(7).subtract(21));
                }
                else{
                    if(time.isBefore(appointment.start())){
                        i=0;
                    }
                    else {
                        i++;
                    }
                    appointmentPane.widthProperty().bind(tv.widthProperty().divide(7).subtract(21+(i*3)));
                }
                time=appointment.end();

                //Height is based on duration
                Duration d = Duration.between(openingHour.withYear(appointment.start().getYear()).withDayOfYear(appointment.start().getDayOfYear()),appointment.start());

                float f = (d.getSeconds()/3600f);

                appointmentPane.setTranslateX(13);
                appointmentPane.translateYProperty().bind(tv.heightProperty().subtract(70).multiply(1f/14f * f).add(31.5));

                d = Duration.between(appointment.start(),appointment.end());
                f = (d.getSeconds()/3600f);

                appointmentPane.heightProperty().bind(tv.heightProperty().subtract(70).multiply(1f/14f * f));

                //Set background to fill a different shade if the appointment is selected.
                appointmentPane.fillProperty().bind(Bindings
                        .when(CalendarController.selectedAppointment.isEqualTo(appointment))
                        .then(Color.hsb(90+(i*28.27735),0.4,1))
                        .otherwise(Color.hsb(90+(i*28.27735),0.6,1)));

                appointmentPane.setOnMouseClicked(mouseEvent -> {
                    CalendarController.INSTANCE.setSelectedAppointment(appointment);
                    mouseEvent.consume();
                });

                Label l = new Label(appointment.customer()+"\n"+appointment.type()+"\n"
                        +appointment.start().format(dtf)+" - "+appointment.end().format(dtf));
                l.setStyle("-fx-font-size:7;");
                l.setOnMouseClicked(mouseEvent -> {
                    CalendarController.INSTANCE.setSelectedAppointment(appointment);
                    mouseEvent.consume();
                });
                l.translateYProperty().bind(appointmentPane.translateYProperty());
                l.setTranslateX(15);

                appointments.add(appointmentPane);
                appointments.add(l);

                background.getChildren().add(appointmentPane);
                background.getChildren().add(l);
            }
        }
    }

    /**
     * <p>Called whenever an object the cell is watching updates.</p>
     * <p>If the object is of type ObservableList, the graphics for the appointments is reset to reflect any changes.</p>
     * <p>Otherwise, if the object is the ZonedDateTime from the Calendar Scene, it first checks to make sure the selected
     * day is in range of this cell, and clears links if it's not. It then checks to see if the selected day matches and
     * updates the background to the cell accordingly.</p>
     * @param observableValue ignored
     * @param o Used for object type
     * @param o1 ignored
     */
    @Override
    public void changed(ObservableValue observableValue, Object o, Object o1) {
        if(o instanceof ObservableList){
            updateAppointmentList();
        }
        else if(o instanceof ZonedDateTime) {
            if(getItem()==null)return;
            ZonedDateTime start = CalendarController.selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            ZonedDateTime end = CalendarController.selectedDay.get().plusDays(1).with(ChronoField.DAY_OF_WEEK,7).truncatedTo(ChronoUnit.DAYS);
            if(getItem().date().isBefore(start)||getItem().date().isAfter(end)) //Check to see if this is outside of the visible days of the calendar, remove listeners if it is.
                clearListeners();
            else if(getItem().date().getDayOfYear()==CalendarController.selectedDay.get().getDayOfYear())
                background.setBackground(selectedBackground);
            else
                background.setBackground(defaultBackground);
        }

    }
}
