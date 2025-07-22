package it.uniroma3.cashlytics.Service.recurrence.strategy;

import java.time.LocalDate;

public class YearlyStrategy implements RecurrenceStrategy {

    @Override
    public LocalDate nextDate(LocalDate date) {
        return date.plusYears(1);
    }

}
