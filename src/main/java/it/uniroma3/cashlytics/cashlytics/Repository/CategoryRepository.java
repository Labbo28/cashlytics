package it.uniroma3.cashlytics.cashlytics.Repository;

import org.springframework.data.repository.CrudRepository;

import it.uniroma3.cashlytics.cashlytics.Model.Category;

public interface CategoryRepository extends CrudRepository<Category, Long> {
}
