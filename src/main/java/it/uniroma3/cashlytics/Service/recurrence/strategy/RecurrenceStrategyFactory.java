package it.uniroma3.cashlytics.Service.recurrence.strategy;

import java.util.Map;

import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;

public class RecurrenceStrategyFactory {
    private static final Map<RecurrencePattern, RecurrenceStrategy> MAP =
        Map.of(
            RecurrencePattern.UNA_TANTUM, new OneTimeStrategy(),
            RecurrencePattern.Giornaliera,      new DailyStrategy(),
            RecurrencePattern.Settimanale,     new WeeklyStrategy(),
            RecurrencePattern.Mensile,    new MonthlyStrategy(),
            RecurrencePattern.Annuale, new YearlyStrategy()
        );

    public static RecurrenceStrategy of(RecurrencePattern type) {
        return MAP.getOrDefault(type, new OneTimeStrategy());
    }
}
