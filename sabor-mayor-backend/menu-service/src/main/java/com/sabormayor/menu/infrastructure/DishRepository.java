package com.sabormayor.menu.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sabormayor.menu.domain.Dish;

public interface DishRepository extends JpaRepository<Dish, UUID> {

    Optional<Dish> findBySlug(String slug);

    boolean existsBySlug(String slug);

    @Query("""
            select distinct d from Dish d
            left join d.tags t
            where (:categorySlug is null or d.category.slug = :categorySlug)
              and (:tag is null or t = :tag)
              and (:onlyAvailable = false or d.available = true)
            order by d.name
            """)
    List<Dish> search(String categorySlug, String tag, boolean onlyAvailable);
}
