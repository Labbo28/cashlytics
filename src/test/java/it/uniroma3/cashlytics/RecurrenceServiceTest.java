package it.uniroma3.cashlytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Service.recurrence.RecurrenceService;

@ExtendWith(MockitoExtension.class)
class RecurrenceServiceTest {

    private final Clock clk = Clock.fixed(
        Instant.parse("2025-07-10T00:00:00Z"),
        ZoneId.of("Europe/Rome")
    );
    private final RecurrenceService svc = new RecurrenceService(clk);

    @Test
    void dailyGeneratesCorrectDates() {
        Transaction t = new Transaction();
        t.setRecurring(true);
        t.setRecurrence(RecurrencePattern.Giornaliera);
        t.setStartDate(LocalDate.of(2025, 7, 1));
        t.setLastGenerated(LocalDate.of(2025, 7, 3));

        List<LocalDate> dates = svc.generateDates(t, LocalDate.of(2025, 7, 6));

        assertEquals(
            List.of(
                LocalDate.of(2025, 7, 4),
                LocalDate.of(2025, 7, 5),
                LocalDate.of(2025, 7, 6)
            ),
            dates
        );
    }

    @Test
    void monthlyRespectsEndDate() {
        Transaction t = new Transaction();
        t.setRecurring(true);
        t.setRecurrence(RecurrencePattern.Mensile);
        t.setStartDate(LocalDate.of(2025, 1, 15));
        t.setLastGenerated(LocalDate.of(2025, 3, 15));
        t.setEndDate(LocalDate.of(2025, 5, 15));

        List<LocalDate> dates = svc.generateDates(t, LocalDate.of(2025, 6, 1));

        assertEquals(
            List.of(
                LocalDate.of(2025, 4, 15),
                LocalDate.of(2025, 5, 15)
            ),
            dates
        );
    }


    @Test
    void oneTimeStrategyYieldsEmpty() {
        Transaction t = new Transaction();
        t.setRecurring(true);
        t.setRecurrence(RecurrencePattern.UNA_TANTUM);
        t.setStartDate(LocalDate.of(2025, 7, 1));
        // lastGenerated rimane null

        List<LocalDate> dates = svc.generateDates(t, LocalDate.of(2025, 7, 31));
        assertTrue(dates.isEmpty());
    }
}
