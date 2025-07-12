package it.uniroma3.cashlytics;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Repository.TransactionRepository;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.recurrence.RecurrenceScheduler;
import it.uniroma3.cashlytics.Service.recurrence.RecurrenceService;

@ExtendWith(MockitoExtension.class)
public class RecurrenceSchedulerTest {

    private TransactionRepository txRepo;
    private RecurrenceService recurrenceService;
    private FinancialAccountService accountService;
    private Clock fixedClock;
    private RecurrenceScheduler scheduler;

    @BeforeEach
    public void setUp() {
        // Fisso “oggi” al 6 luglio 2025
        fixedClock = Clock.fixed(
            Instant.parse("2025-07-06T00:00:00Z"),
            ZoneId.of("Europe/Rome")
        );
        txRepo           = mock(TransactionRepository.class);
        recurrenceService= new RecurrenceService(fixedClock);
        accountService   = mock(FinancialAccountService.class);

        scheduler = new RecurrenceScheduler(
            txRepo,
            recurrenceService,
            accountService,
            fixedClock
        );
    }

    @Test
    public void runDaily_appliesAllDailyRecurrencesAndUpdatesLastGenerated() {
        // 1) Template di transazione ricorrente
        Transaction template = new Transaction();
        template.setId(1L);
        template.setRecurring(true);
        template.setRecurrence(RecurrencePattern.Giornaliera);
        template.setStartDate(LocalDate.of(2025, 7, 1));
        template.setLastGenerated(LocalDate.of(2025, 7, 3));
        template.setAmount(new BigDecimal("50.00"));

        when(txRepo.findAllByIsRecurringTrue())
            .thenReturn(List.of(template));

        // 2) Invoco lo scheduler
        scheduler.runDaily();

        // 3) Verifico che postTransactionAndUpdateBalance venga invocato 3 volte
        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        verify(accountService, times(3))
            .postTransactionAndUpdateBalance(captor.capture());

        List<Transaction> created = captor.getAllValues();
        assertEquals(LocalDate.of(2025,7,4), created.get(0).getStartDate());
        assertEquals(LocalDate.of(2025,7,5), created.get(1).getStartDate());
        assertEquals(LocalDate.of(2025,7,6), created.get(2).getStartDate());

        // 4) lastGenerated sulla template deve essere aggiornato al 6 luglio
        assertEquals(LocalDate.of(2025,7,6), template.getLastGenerated());

        // 5) Verifico che txRepo.save(template) sia chiamato
        verify(txRepo).save(template);
    }
}
