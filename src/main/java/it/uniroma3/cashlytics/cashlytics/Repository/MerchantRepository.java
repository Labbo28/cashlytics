package it.uniroma3.cashlytics.cashlytics.Repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.cashlytics.Model.User;

public interface MerchantRepository extends CrudRepository<Merchant, Long> {

    Set<Merchant> findAllByUser(User currentUser);

    Optional<Merchant> findByIdAndUser(Long merId, User user);

   Optional<Merchant> findByNameIgnoreCaseAndUser(String name, User user);
   
}
