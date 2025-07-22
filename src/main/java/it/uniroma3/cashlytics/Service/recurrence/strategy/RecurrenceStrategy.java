package it.uniroma3.cashlytics.Service.recurrence.strategy;

import java.time.LocalDate;

public interface RecurrenceStrategy {
    public LocalDate nextDate(LocalDate date);

}
