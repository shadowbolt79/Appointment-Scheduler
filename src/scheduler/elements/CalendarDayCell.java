package scheduler.elements;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import scheduler.records.Appointment;
import scheduler.controllers.CalendarController;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

/**
 * <p>Handles graphical updates and mouse clicks for the table cell in Calendar View.</p>
 * <p>Clicking on the time for an appointment will select it to be viewed/edited/copied. Clicking anywhere else in the
 * cell will select that date.</p>
 * @author Ken Butler
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class CalendarDayCell extends TableCell<CalendarWeek, CalendarDay> implements ChangeListener {
    private final Label l_date;
    private final AnchorPane background;
    private final TableView<CalendarWeek> parent;

    private final ArrayList<Label> appointments = new ArrayList<>();

    private static final Background defaultBackground = new Background(new BackgroundFill(Color.rgb(255,255,255),new CornerRadii(0), Insets.EMPTY));
    private static final Background outOfMonthBackground = new Background(new BackgroundFill(Color.rgb(211,215,215),new CornerRadii(0), Insets.EMPTY));
    private static final Background selectedBackground = new Background(new BackgroundFill(Color.rgb(185,185,255),new CornerRadii(0), Insets.EMPTY));
    private static final Background selectedAppBackground = new Background(new BackgroundFill(Color.rgb(215,215,255),new CornerRadii(0), Insets.EMPTY));

    SimpleListProperty<Appointment> observableList;

    /**
     * Binds the size and shape of the cell so window can be resized. Adds functionality to click to set day.
     * @param parent The tableview holding this cell.
     */
    public CalendarDayCell (TableView<CalendarWeek> parent) {
        background = new AnchorPane();
        this.parent = parent;
        l_date = new Label();
        l_date.relocate(5,5);

        background.getChildren().add(l_date);
        setGraphic(background);

        prefHeightProperty().bind(parent.heightProperty().add(-25).divide(parent.itemsProperty().get().size()));
        prefWidthProperty().bind(parent.widthProperty().divide(7));

        setOnMouseClicked(mouseEvent -> {
            if(this.getItem()!=null) {
                CalendarController.INSTANCE.setDay(getItem().date());
            }
        });
    }

    /**
     * Sets the cell to watch a particular day.
     * @param calendarDay The Day
     * @param b unused
     */
    @Override
    protected void updateItem(CalendarDay calendarDay, boolean b) {
        super.updateItem(calendarDay, b);
        if(calendarDay!=null&&!b) {
            updateAppointmentList();
        }

        //Remove listener if exists
        if(observableList!=null) {
            observableList.removeListener(this);
            observableList = null;
        }

        //Add listener if valid and set background
        if(calendarDay!=null&&calendarDay.appointments()!=null) {
            observableList = calendarDay.appointments();
            observableList.addListener(this);

            CalendarController.selectedDay.addListener(this);

            prefHeightProperty().bind(parent.heightProperty().add(-25).divide(parent.itemsProperty().get().size()));

            if (getItem() == null) return;
            if (getItem().date().getMonthValue() != CalendarController.selectedDay.get().getMonthValue()) {
                background.setBackground(outOfMonthBackground);
            } else if (getItem().date().getDayOfYear() == CalendarController.selectedDay.get().getDayOfYear())
                background.setBackground(selectedBackground);
            else
                background.setBackground(defaultBackground);

        }
        else {
            CalendarController.selectedDay.removeListener(this);
        }
    }

    /**
     * Removes listener to the selected day and appointment list.
     */
    private void clearListeners(){
        if(observableList!=null)
            observableList.removeListener(this);
        CalendarController.selectedDay.removeListener(this);
    }

    /**
     * Resets graphical labels for active appointments.
     * <p><p>
     * Called when the appointment list is changed or updated.
     */
    private void updateAppointmentList() {
        if(this.getItem()!=null)
        {
            l_date.setText(getItem().date().getDayOfMonth()+"");
            for(Label l:appointments){
                background.getChildren().remove(l);
            }
            appointments.clear();
            if(!getItem().appointments().isEmpty()) {
                int i = 2;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
                Label l = new Label(getItem().appointments().size()+" Appointments");
                l.setStyle("-fx-font-size: 6pt;");
                l.relocate(10, 18);
                appointments.add(l);
                background.getChildren().add(l);
                for (Appointment a : this.getItem().appointments()) {

                    l = new Label("(" + a.start().format(dtf) + " - " + a.end().format(dtf) + ")");
                    l.setStyle("-fx-font-size: 6pt;");
                    l.setOnMouseClicked(event -> {
                        CalendarController.INSTANCE.setSelectedAppointment(a);
                        event.consume();
                    });
                    l.relocate(10, 7 + 11 * (i++));
                    l.backgroundProperty().bind(Bindings.when(CalendarController.selectedAppointment.isEqualTo(a)).then(selectedAppBackground).otherwise(Background.EMPTY));
                    appointments.add(l);
                    background.getChildren().add(l);
                }
            }
        }
        else
            l_date.setText("");
    }

    /**
     * <p>Called whenever one of the observable objects updates.</p>
     * <p>Will either be the list of appointments, or the calendar's selected date.</p>
     * <p>An update to the list will cause the visible list of appointments to reset.</p>
     * <p>An update to the selected date will first check to ensure the date isn't outside of what's visible on the calendar,
     * then recolor the background to the cell.</p>
     * @param observableValue ignored
     * @param o Type is checked to determine what update has occurred.
     * @param o1 ignored
     */
    @Override
    public void changed(ObservableValue observableValue, Object o, Object o1) {
        if(o instanceof ObservableList)
            updateAppointmentList();
        else if(o instanceof ZonedDateTime) {
            if(getItem()==null)return;
            ZonedDateTime start = CalendarController.selectedDay.get().withDayOfMonth(2).with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1).truncatedTo(ChronoUnit.DAYS);
            ZonedDateTime end = CalendarController.selectedDay.get().withDayOfMonth(1).plusMonths(1).with(ChronoField.DAY_OF_WEEK,6).plusDays(1).truncatedTo(ChronoUnit.DAYS);
            if(getItem().date().isBefore(start)||getItem().date().isAfter(end)) //Check to see if this is outside of the visible days of the calendar, remove listeners if it is.
                clearListeners();
            else if(getItem().date().getMonthValue()!=CalendarController.selectedDay.get().getMonthValue())
            {
                background.setBackground(outOfMonthBackground);
            }
            else if(getItem().date().getDayOfYear()==CalendarController.selectedDay.get().getDayOfYear())
                background.setBackground(selectedBackground);
            else
                background.setBackground(defaultBackground);
        }
    }
}
