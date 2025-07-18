package it.uniroma3.cashlytics.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.TransactionService;
import it.uniroma3.cashlytics.Service.UserService;
import it.uniroma3.cashlytics.Service.MerchantService;
import jakarta.validation.Valid;

@Controller
public class TransactionController {

	@Autowired
	private TransactionService transactionService;
	@Autowired
	private FinancialAccountService financialAccountService;
	@Autowired
	private UserService userService;
	@Autowired
	private MerchantService merchantService;

	@PostMapping("/{username}/account/{accountId}/add-transaction")
	@Transactional
	public String addTransaction(
			@PathVariable String username,
			@PathVariable Long accountId,
			@Valid TransactionDTO transactionDTO,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (transactionDTO.getDate() == null) {
			transactionDTO.setDate(LocalDate.now());
		}

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.transactionDTO", bindingResult);
			redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
			redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
		User user = userService.getUserByUsername(username);

		Transaction newTransaction = transactionService.createTransaction(transactionDTO, account, user);

		if (newTransaction == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to create transaction.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
		return "redirect:/" + username + "/account/" + accountId;
	}

	@GetMapping("/{username}/account/{accountId}/recurring")
	public String showRecurringTransactions(
			@PathVariable String username,
			@PathVariable Long accountId,
			Model model) {

		Optional<FinancialAccount> accountOpt = financialAccountService.findById(accountId);

		if (accountOpt.isEmpty()) {
			model.addAttribute("errorMessage", "Account not found.");
			return "redirect:/" + username + "/dashboard";
		}

		FinancialAccount account = accountOpt.get();
		Set<Transaction> all = account.getTransactions();

		List<Transaction> recurring = all.stream()
				.filter(tx -> tx.getRecurrence() != RecurrencePattern.UNA_TANTUM)
				.toList();

		model.addAttribute("account", account);
		model.addAttribute("transactions", recurring);
		model.addAttribute("username", username);

		return "recurring-transactions";
	}

	@PostMapping("/{username}/account/{accountId}/delete-transaction/{transactionId}")
	public String deleteTransaction(
			@PathVariable String username,
			@PathVariable Long accountId,
			@PathVariable Long transactionId,
			RedirectAttributes redirectAttributes) {

		try {
			transactionService.deleteTransaction(transactionId);
			redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error deleting transaction: " + e.getMessage());
		}

		return "redirect:/" + username + "/account/" + accountId;
	}

	@GetMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
	public String editTransactionForm(
			@PathVariable String username,
			@PathVariable Long accountId,
			@PathVariable Long transactionId,
			Model model,
			RedirectAttributes redirectAttributes) {

		Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
		if (transactionOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		Transaction transaction = transactionOpt.get();
		FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
		User user = userService.getUserByUsername(username);

		TransactionDTO transactionDTO = new TransactionDTO();
		transactionDTO.setAmount(transaction.getAmount());
		transactionDTO.setDescription(transaction.getDescription());
		transactionDTO.setDate(transaction.getStartDate());
		transactionDTO.setRecurrencePattern(transaction.getRecurrence());
		if (transaction.getMerchant() != null) {
			transactionDTO.setMerchantId(transaction.getMerchant().getId());
		}

		model.addAttribute("transaction", transaction);
		model.addAttribute("transactionDTO", transactionDTO);
		model.addAttribute("account", account);
		model.addAttribute("username", username);
		model.addAttribute("merchants", merchantService.findAllByUser(user));

		return "edit-transaction";
	}

	@PostMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
	public String editTransaction(
			@PathVariable String username,
			@PathVariable Long accountId,
			@PathVariable Long transactionId,
			@ModelAttribute("transactionDTO") @Valid TransactionDTO transactionDTO,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {

		if (bindingResult.hasErrors()) {
			Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
			if (transactionOpt.isPresent()) {
				Transaction transaction = transactionOpt.get();
				FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
				User user = userService.getUserByUsername(username);

				model.addAttribute("transaction", transaction);
				model.addAttribute("account", account);
				model.addAttribute("username", username);
				model.addAttribute("transactionId", transactionId);
				model.addAttribute("merchants", merchantService.findAllByUser(user));
			}
			return "edit-transaction";
		}

		Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
		if (transactionOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		Transaction transaction = transactionOpt.get();
		User user = userService.getUserByUsername(username);

		transactionService.updateTransaction(transaction, transactionDTO, transaction.getAmount(), user);
		redirectAttributes.addFlashAttribute("successMessage", "Transazione aggiornata con successo.");
		return "redirect:/" + username + "/account/" + accountId;
	}

}