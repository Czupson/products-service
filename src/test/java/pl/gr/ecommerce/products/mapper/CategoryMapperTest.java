package pl.gr.ecommerce.products.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.gr.ecommerce.products.category.Category;
import pl.gr.ecommerce.products.dto.category.CategoryResponse;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {
    private CategoryMapper categoryMapper;
    private Category category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryMapper = new CategoryMapperImpl();
        categoryId = UUID.randomUUID();
        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");
    }

    @Test
    void shouldMapCategoryWithoutParent() {
        CategoryResponse result = categoryMapper.toResponse(category);
        assertNotNull(result);
        assertEquals(categoryId, result.id());
        assertEquals("Electronics", result.name());
        assertNull(result.parentId());
        assertNotNull(result.children());
        assertTrue(result.children().isEmpty());
    }

    @Test
    void shouldMapCategoryWithParent() {
        UUID parentId = UUID.randomUUID();
        Category parent = new Category();
        parent.setId(parentId);
        parent.setName("Main category");
        category.setParent(parent);
        CategoryResponse result = categoryMapper.toResponse(category);
        assertEquals(parentId, result.parentId());
    }

    @Test
    void shouldMapCategoryWithChildren() {
        Category child = new Category();
        child.setId(UUID.randomUUID());
        child.setName("Smartphones");
        child.setParent(category);
        category.setChildren(List.of(child));
        CategoryResponse result = categoryMapper.toResponse(category);
        assertEquals(1, result.children().size());
        assertEquals("Smartphones", result.children().get(0).name());
        assertEquals(categoryId, result.children().get(0).parentId());
    }

    @Test
    void shouldMapListOfCategories() {
        Category secondCategory = new Category();
        secondCategory.setId(UUID.randomUUID());
        secondCategory.setName("Computers");
        List<Category> categories = List.of(category, secondCategory);
        List<CategoryResponse> result =
                categoryMapper.toResponseList(categories);
        assertEquals(2, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals("Computers", result.get(1).name());
    }
}
