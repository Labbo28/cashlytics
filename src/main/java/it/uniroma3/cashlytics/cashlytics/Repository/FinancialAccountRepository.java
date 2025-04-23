package it.uniroma3.cashlytics.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.FinancialAccount;

public interface FinancialAccountRepository extends CrudRepository<FinancialAccount, Long> {
 
}
