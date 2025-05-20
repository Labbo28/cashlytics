package it.uniroma3.cashlytics.cashlytics.Repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.User;

public interface UserRepository extends CrudRepository<User,Long>{

    Optional<User> findByEmail(String email);

}
