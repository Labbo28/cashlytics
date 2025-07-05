package it.uniroma3.cashlytics.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MerchantService merchantService;

    /**
     * Crea una nuova transazione, gestendo anche category e merchant:
     * - Se dto.getCategoryId() è valorizzato, recupera quella categoria per
     * l'utente.
     * - Altrimenti, se dto.getCategoryName() non è vuoto, cerca o crea la
     * categoria.
     * - Stessa logica per Merchant.
     * - Aggiorna il bilancio del FinancialAccount.
     * - Salva la transazione (e eventuali nuove Category/Merchant) nel repository.
     *
     * @param transactionDTO dati dal form (importo, tipo, descrizione,
     *                       categoryId/name, merchantId/name)
     * @param account        l'account finanziario a cui associare la transazione
     * @param user           l'utente corrente (proprietario di account, category e
     *                       merchant)
     * @param bindingResult  per segnalare eventuali errori di validazione su
     *                       category/merchant
     * @return la transazione salvata, oppure null se ci sono errori
     */
    public Transaction createTransaction(
            TransactionDTO transactionDTO,
            FinancialAccount account,
            User user,
            BindingResult bindingResult) {

        // 1. Risolvi o crea Category
        Category category = resolveOrCreateCategory(transactionDTO, user, bindingResult);
        // 2. Risolvi o crea Merchant
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user, bindingResult);

        // 3. Se ci sono errori di binding (invalid category/merchant) interrompi
        if (bindingResult.hasErrors()) {
            return null;
        }

        // 4. Costruisci e popola la nuova Transaction
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transactionDTO.getAmount());
        newTransaction.setDescription(transactionDTO.getDescription());
        newTransaction.setTransactionType(transactionDTO.getTransactionType());
        newTransaction.setDate(LocalDateTime.now());
        newTransaction.setFinancialAccount(account);
        newTransaction.setCategory(category);
        newTransaction.setMerchant(merchant);

        // 5. Aggiungi la transazione alla lista dell'account
        account.getTransactions().add(newTransaction);

        // 6. Aggiorna il bilancio dell'account
        boolean isIncome = transactionDTO.getTransactionType() == TransactionType.INCOME;
        if (isIncome) {
            account.setBalance(account.getBalance().add(transactionDTO.getAmount()));
        } else {
            account.setBalance(account.getBalance().subtract(transactionDTO.getAmount()));
        }

        // 7. Salva transaction (e category/merchant già salvate all'occorrenza)
        transactionRepository.save(newTransaction);
        return newTransaction;
    }

    private Category resolveOrCreateCategory(
            TransactionDTO dto,
            User user,
            BindingResult bindingResult) {

        Long catId = dto.getCategoryId();
        String catName = dto.getCategoryName() != null ? dto.getCategoryName().trim() : "";

        if (catId != null) {
            Optional<Category> opt = categoryService.findByIdAndUser(catId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue(
                        "categoryName",
                        "error.transactionDTO",
                        "Invalid category selected.");
                return null;
            }
        }

        if (!catName.isEmpty()) {
            Optional<Category> optByName = categoryService.findByNameAndUser(catName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                Category newCat = new Category();
                newCat.setName(catName);
                newCat.setUser(user);
                return categoryService.save(newCat);
            }
        }

        bindingResult.rejectValue(
                "categoryName",
                "error.transactionDTO",
                "Category is required.");
        return null;
    }

    private Merchant resolveOrCreateMerchant(
            TransactionDTO dto,
            User user,
            BindingResult bindingResult) {

        Long merId = dto.getMerchantId();
        String merName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";

        if (merId != null) {
            Optional<Merchant> opt = merchantService.findByIdAndUser(merId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue(
                        "merchantName",
                        "error.transactionDTO",
                        "Invalid merchant selected.");
                return null;
            }
        }

        if (!merName.isEmpty()) {
            Optional<Merchant> optByName = merchantService.findByNameAndUser(merName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                Merchant newMer = new Merchant();
                newMer.setName(merName);
                newMer.setUser(user);
                return merchantService.save(newMer);
            }
        }

        bindingResult.rejectValue(
                "merchantName",
                "error.transactionDTO",
                "Merchant is required.");
        return null;
    }

}
