package it.uniroma3.cashlytics.Repository;

import java.util.Optional;
import java.util.Set;
import org.springframework.data.repository.CrudRepository;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.User;

public interface CategoryRepository extends CrudRepository<Category, Long> {

	Set<Category> findAllByUser(User currentUser);

	Optional<Category> findByIdAndUser(Long catId, User user);

	Optional<Category> findByNameIgnoreCaseAndUser(String catName, User user);

}
