package pl.gr.ecommerce.products.service;

import pl.gr.ecommerce.products.category.Category;
import pl.gr.ecommerce.products.dto.category.CategoryResponse;
import pl.gr.ecommerce.products.exception.category.CategoryAlreadyExistsException;
import pl.gr.ecommerce.products.exception.category.CategoryNotFoundException;
import pl.gr.ecommerce.products.exception.category.InvalidCategoryException;
import pl.gr.ecommerce.products.mapper.CategoryMapper;
import pl.gr.ecommerce.products.repository.CategoryRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;
    private Category category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");
        category.setChildren(new ArrayList<>());
    }

    @Test
    void shouldCreateCategory() {
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                null,
                List.of()
        );
        when(categoryMapper.toResponse(any(Category.class))).thenReturn(expectedResponse);
        CategoryResponse result = categoryService.createCategory(
                "Electronics", null
        );
        assertNotNull(result);
        assertEquals("Electronics", result.name());
        assertNull(result.parentId());
        verify(categoryRepository).save(any(Category.class));
        verify(categoryMapper).toResponse(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryNameIsEmpty() {
        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.createCategory(" ", null)
        );
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void shouldThrowExceptionWhenCategoryAlreadyExists() {
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.createCategory("Electronics", null)
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                null,
                List.of()
        );
        when(categoryMapper.toResponseList(List.of(category))).thenReturn(List.of(expectedResponse));
        List<CategoryResponse> result = categoryService.getAllCategories();
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        verify(categoryMapper).toResponseList(List.of(category));
    }

    @Test
    void shouldReturnCategoryById() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                null,
                List.of()
        );
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);
        CategoryResponse result = categoryService.getCategoryById(categoryId);
        assertEquals(categoryId, result.id());
        assertEquals("Electronics", result.name());
        verify(categoryMapper).toResponse(category);
    }


    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExist() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getCategoryById(categoryId)
        );
    }

    @Test
    void shouldReturnRootCategories() {
        when(categoryRepository.findByParentIsNull()).thenReturn(List.of(category));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                null,
                List.of()
        );
        when(categoryMapper.toResponseList(List.of(category))).thenReturn(List.of(expectedResponse));
        List<CategoryResponse> result = categoryService.getRootCategories();
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertNull(result.get(0).parentId());
        verify(categoryMapper).toResponseList(List.of(category));
    }

    @Test
    void shouldReturnSubcategories() {
        UUID parentId = UUID.randomUUID();
        when(categoryRepository.existsById(parentId)).thenReturn(true);
        when(categoryRepository.findByParent_Id(parentId)).thenReturn(List.of(category));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                parentId,
                List.of()
        );
        when(categoryMapper.toResponseList(List.of(category))).thenReturn(List.of(expectedResponse));
        List<CategoryResponse> result = categoryService.getSubcategories(parentId);
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).name());
        assertEquals(parentId, result.get(0).parentId());
        verify(categoryMapper).toResponseList(List.of(category));
    }

    @Test
    void shouldThrowExceptionWhenParentDoesNotExist() {
        UUID parentId = UUID.randomUUID();
        when(categoryRepository.existsById(parentId)).thenReturn(false);
        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.getSubcategories(parentId)
        );
    }

    @Test
    void shouldDeleteCategoryWithoutChildren() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        categoryService.deleteCategory(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    void shouldNotDeleteCategoryWithChildren() {
        category.getChildren().add(new Category());
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.deleteCategory(categoryId)
        );
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void shouldUpdateCategoryName() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Computers")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Computers",
                null,
                List.of()
        );
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);
        CategoryResponse result = categoryService.updateCategory(
                categoryId, "Computers", null
        );
        assertEquals("Computers", result.name());
        assertNull(result.parentId());
        assertNull(category.getParent());
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void shouldUpdateCategoryParent() {
        UUID parentId = UUID.randomUUID();
        Category parent = new Category();
        parent.setId(parentId);
        parent.setName("Electronics");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Laptops")).thenReturn(Optional.empty());
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Laptops",
                parentId,
                List.of()
        );
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);
        CategoryResponse result = categoryService.updateCategory(
                categoryId, "Laptops", parentId
        );
        assertEquals(parentId, result.parentId());
        assertEquals(parentId, category.getParent().getId());
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void shouldMoveCategoryToRoot() {
        Category parent = new Category();
        parent.setId(UUID.randomUUID());
        parent.setName("Electronics");
        category.setParent(parent);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CategoryResponse expectedResponse = new CategoryResponse(
                categoryId,
                "Electronics",
                null,
                List.of()
        );
        when(categoryMapper.toResponse(category)).thenReturn(expectedResponse);
        CategoryResponse result = categoryService.updateCategory(
                categoryId, "Electronics", null
        );
        assertNull(category.getParent());
        assertNull(result.parentId());
        verify(categoryRepository).save(category);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithEmptyName() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.updateCategory(categoryId, " ", null)
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenUpdatingWithExistingName() {
        UUID otherId = UUID.randomUUID();
        Category otherCategory = new Category();
        otherCategory.setId(otherId);
        otherCategory.setName("Computers");
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Computers")).thenReturn(Optional.of(otherCategory));
        assertThrows(
                CategoryAlreadyExistsException.class,
                () -> categoryService.updateCategory(
                        categoryId, "Computers", null
                )
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenCategoryIsItsOwnParent() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.updateCategory(
                        categoryId, "Electronics", categoryId
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenCreatingCategoryCycle() {
        UUID childId = UUID.randomUUID();
        Category child = new Category();
        child.setId(childId);
        child.setName("Computers");
        child.setParent(category);
        category.getChildren().add(child);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Electronics")).thenReturn(Optional.of(category));
        when(categoryRepository.findById(childId)).thenReturn(Optional.of(child));
        assertThrows(
                InvalidCategoryException.class,
                () -> categoryService.updateCategory(
                        categoryId, "Electronics", childId
                )
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void shouldThrowExceptionWhenNewParentDoesNotExist() {
        UUID parentId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Laptops")).thenReturn(Optional.empty());
        when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());
        assertThrows(
                CategoryNotFoundException.class,
                () -> categoryService.updateCategory(
                        categoryId, "Laptops", parentId
                )
        );
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
