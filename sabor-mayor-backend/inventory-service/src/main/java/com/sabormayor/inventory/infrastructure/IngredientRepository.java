package com.sabormayor.inventory.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sabormayor.inventory.domain.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, UUID> {

    @Query("select i from Ingredient i where i.stockQuantity <= i.minStock order by i.name")
    List<Ingredient> findBelowMinimum();
}
