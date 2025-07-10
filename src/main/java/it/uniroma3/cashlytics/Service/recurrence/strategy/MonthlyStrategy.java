package it.uniroma3.cashlytics.Service.recurrence.strategy;

import java.time.LocalDate;

public class MonthlyStrategy implements RecurrenceStrategy{

    @Override
    public LocalDate nexDate(LocalDate date) {
        return date.plusMonths(1);
    }

}
