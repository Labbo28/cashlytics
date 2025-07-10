package it.uniroma3.cashlytics.Service.recurrence;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Repository.TransactionRepository;
import it.uniroma3.cashlytics.Service.FinancialAccountService;

@Service
public class RecurrenceScheduler {

    private final TransactionRepository txRepo;
    private final RecurrenceService recurrenceService;
    private final FinancialAccountService accountService;
    private final Clock clock;

    public RecurrenceScheduler(TransactionRepository txRepo,
                               RecurrenceService recurrenceService,
                               FinancialAccountService accountService,
                               Clock clock) {
        this.txRepo = txRepo;
        this.recurrenceService = recurrenceService;
        this.accountService = accountService;
        this.clock = clock;
    }

    /** Eseguito ogni giorno a mezzanotte */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void runDaily() {
        LocalDate today = LocalDate.now(clock);

        List<Transaction> templates = txRepo.findAllByIsRecurringTrue();
        for (Transaction tpl : templates) {
            List<LocalDate> dates = recurrenceService.generateDates(tpl, today);
            for (LocalDate date : dates) {
                Transaction live = tpl.cloneForDate(date);
                accountService.postTransactionAndUpdateBalance(live);
                tpl.setLastGenerated(date);
            }
            txRepo.save(tpl);
        }
    }
}
