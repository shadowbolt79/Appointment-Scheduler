package scheduler.elements;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

/**
 * Holds a week of {@link CalendarDay}s.
 */
public class CalendarWeek {
    private final ObjectProperty<CalendarDay> sunday, monday, tuesday, wednesday, thursday, friday, saturday;

    /**
     * Generates a week of days based on the {@link ZonedDateTime}. Week is considered Sun-Sat instead of Java's Mon-Sun.
     * @param week A time that is inside the requested week.
     */
    public CalendarWeek(ZonedDateTime week) {
        ZonedDateTime sun = week.plusDays(1).with(ChronoField.DAY_OF_WEEK,7).minusWeeks(1);
        this.sunday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun));
        this.monday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(1)));
        this.tuesday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(2)));
        this.wednesday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(3)));
        this.thursday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(4)));
        this.friday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(5)));
        this.saturday = new SimpleObjectProperty<>(new CalendarDay(new SimpleListProperty<>(FXCollections.observableList(new ArrayList<>())),sun.plusDays(6)));
    }

    /**
     * Returns the {@link CalendarDay} that represents Sunday.
     * @return Sunday
     */
    public final CalendarDay getSunday() {
        return sunday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Monday.
     * @return Monday
     */
    public final CalendarDay getMonday() {
        return monday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Tuesday.
     * @return Tuesday
     */
    public final CalendarDay getTuesday() {
        return tuesday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Wednesday.
     * @return Wednesday
     */
    public final CalendarDay getWednesday() {
        return wednesday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Thursday.
     * @return Thursday
     */
    public final CalendarDay getThursday() {
        return thursday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Friday.
     * @return Friday
     */
    public final CalendarDay getFriday() {
        return friday.get();
    }
    /**
     * Returns the {@link CalendarDay} that represents Saturday.
     * @return Saturday
     */
    public final CalendarDay getSaturday() {
        return saturday.get();
    }
}