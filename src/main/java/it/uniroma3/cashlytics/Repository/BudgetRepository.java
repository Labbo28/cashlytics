package it.uniroma3.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.Model.Budget;

public interface BudgetRepository extends CrudRepository<Budget, Long> {
}
