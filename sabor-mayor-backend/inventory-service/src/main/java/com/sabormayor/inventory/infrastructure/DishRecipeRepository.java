package com.sabormayor.inventory.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.inventory.domain.DishRecipe;

public interface DishRecipeRepository extends JpaRepository<DishRecipe, UUID> {

    List<DishRecipe> findByDishId(UUID dishId);
}
