package it.uniroma3.cashlytics.Model;

import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Data
public class Category {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String icon; // as Url
    private String color; // as Hex

    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "category")
    private Set<Transaction> transactions;
    @EqualsAndHashCode.Exclude
    @ManyToOne
    private User user;

    // Relazione auto-referenziale
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Category> subCategories;
    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

}
