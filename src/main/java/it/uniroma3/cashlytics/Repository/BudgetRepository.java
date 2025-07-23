package it.uniroma3.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Category;
import java.util.List;

public interface BudgetRepository extends CrudRepository<Budget, Long> {
    Budget findByFinancialAccountAndCategory(FinancialAccount account, Category category);
    List<Budget> findAllByFinancialAccountAndCategory(FinancialAccount account, Category category);
}
