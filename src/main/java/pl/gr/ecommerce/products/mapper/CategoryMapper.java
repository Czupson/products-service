package pl.gr.ecommerce.products.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.gr.ecommerce.products.category.Category;
import pl.gr.ecommerce.products.dto.category.CategoryResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);
    List<CategoryResponse> toResponseList(List<Category> categories);
}
