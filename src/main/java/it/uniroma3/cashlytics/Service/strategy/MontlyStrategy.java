package it.uniroma3.cashlytics.Service.strategy;

import java.time.LocalDate;

public class MontlyStrategy implements RecurrenceStrategy{

    @Override
    public LocalDate nexDate(LocalDate date) {
        return date.plusMonths(1);
    }

}
