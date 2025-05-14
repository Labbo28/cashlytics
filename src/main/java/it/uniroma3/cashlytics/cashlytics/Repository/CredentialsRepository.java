package it.uniroma3.cashlytics.cashlytics.Repository;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


public interface CredentialsRepository  extends CrudRepository<Credentials, Long> {
  
   Optional <Credentials> findByUsername(String username);





}
