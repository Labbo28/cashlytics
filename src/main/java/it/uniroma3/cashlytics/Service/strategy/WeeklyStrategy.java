package it.uniroma3.cashlytics.Service.strategy;

import java.time.LocalDate;

public class WeeklyStrategy implements RecurrenceStrategy {

    @Override
    public LocalDate nexDate(LocalDate date) {
        return date.plusWeeks(1);
    }

}
