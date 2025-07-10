package it.uniroma3.cashlytics.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.Model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

    List<Transaction> findAllByIsRecurringTrue();

}
