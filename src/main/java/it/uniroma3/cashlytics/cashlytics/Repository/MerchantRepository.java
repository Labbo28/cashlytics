package it.uniroma3.cashlytics.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.Merchant;

public interface MerchantRepository extends CrudRepository<Merchant, Long> {
   
}
