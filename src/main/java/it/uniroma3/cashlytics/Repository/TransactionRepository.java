package it.uniroma3.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.Model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {

}
