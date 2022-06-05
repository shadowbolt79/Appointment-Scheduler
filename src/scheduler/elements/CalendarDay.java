package scheduler.elements;

import javafx.beans.property.SimpleListProperty;
import scheduler.records.Appointment;

import java.time.ZonedDateTime;

/**
 * Holds the appointments and date of a single day. Useful for keeping information for the Calendar View.
 */
public record CalendarDay(SimpleListProperty<Appointment> appointments, ZonedDateTime date) { }
