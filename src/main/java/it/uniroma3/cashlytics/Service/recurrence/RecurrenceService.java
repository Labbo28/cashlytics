package it.uniroma3.cashlytics.Service.recurrence;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Service.recurrence.strategy.RecurrenceStrategy;
import it.uniroma3.cashlytics.Service.recurrence.strategy.RecurrenceStrategyFactory;

@Service
public class RecurrenceService {

    private final Clock clock;

    public RecurrenceService(Clock clock) {
        this.clock = clock;
    }

    /**
     * Calcola le date mancanti di ricorrenza per la transaction `tx`
     * fino a `to` (incluso).
     */
    public List<LocalDate> generateDates(Transaction tx, LocalDate to) {
        if (!tx.isRecurring())
            return List.of();

        LocalDate cursor = Optional.ofNullable(tx.getLastGenerated())
                .orElse(tx.getStartDate());
        RecurrenceStrategy strategy = RecurrenceStrategyFactory.of(tx.getRecurrence());

        List<LocalDate> dates = new ArrayList<>();
        LocalDate next = strategy.nexDate(cursor);

        while (next != null
                && !next.isAfter(to)
                && (tx.getEndDate() == null || !next.isAfter(tx.getEndDate()))) {
            dates.add(next);
            cursor = next;
            next = strategy.nexDate(cursor);
        }
        return dates;
    }
}
