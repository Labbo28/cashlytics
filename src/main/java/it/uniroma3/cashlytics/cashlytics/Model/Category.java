package it.uniroma3.cashlytics.cashlytics.Model;

import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String icon;
    private String color;
    
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions;
    @EqualsAndHashCode.Exclude
    @ManyToOne 
    private User user;

}
