package it.uniroma3.cashlytics.Service.recurrence.strategy;

import java.time.LocalDate;

public class OneTimeStrategy implements RecurrenceStrategy {

    @Override
    public LocalDate nexDate(LocalDate date) {
      return null; // One-time events do not recur, so we return null
    }
    

}
