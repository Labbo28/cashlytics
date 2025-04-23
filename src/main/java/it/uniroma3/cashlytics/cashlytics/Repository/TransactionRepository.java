package it.uniroma3.cashlytics.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.Transaction;

public interface TransactionRepository extends CrudRepository<Transaction,Long>{

}
