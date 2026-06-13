package com.sabormayor.menu.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sabormayor.menu.domain.Category;
import com.sabormayor.menu.domain.Dish;
import com.sabormayor.menu.web.dto.CategoryResponse;
import com.sabormayor.menu.web.dto.DishMarginResponse;
import com.sabormayor.menu.web.dto.DishResponse;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "categorySlug", source = "category.slug")
    @Mapping(target = "categoryName", source = "category.name")
    DishResponse toResponse(Dish dish);

    List<DishResponse> toDishResponses(List<Dish> dishes);

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toCategoryResponses(List<Category> categories);

    @Mapping(target = "marginPercent", expression = "java(dish.marginPercent())")
    DishMarginResponse toMarginResponse(Dish dish);

    List<DishMarginResponse> toMarginResponses(List<Dish> dishes);
}
