package it.uniroma3.cashlytics.Service.strategy;

import java.time.LocalDate;

public interface RecurrenceStrategy {
    public LocalDate nexDate(LocalDate date);

}
