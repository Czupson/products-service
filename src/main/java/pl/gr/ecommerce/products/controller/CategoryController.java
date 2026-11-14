package pl.gr.ecommerce.products.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import pl.gr.ecommerce.products.dto.category.UpdateCategoryRequest;
import pl.gr.ecommerce.products.service.CategoryService;
import pl.gr.ecommerce.products.dto.category.CreateCategoryRequest;
import pl.gr.ecommerce.products.dto.category.CategoryResponse;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Endpoints for managing product categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Create category",
            description = "Creates a new category with an optional parent category")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @Valid @RequestBody CreateCategoryRequest request
    ) {
        log.info("REST request to create category: {}", request.name());
        CategoryResponse response = categoryService.createCategory(
                request.name(),
                request.parentId()
        );
        log.info("Category created successfully with ID: {}", response.id());
        return response;
    }

    @Operation(summary = "Get all categories",
            description = "Returns a list of all product categories")
    @GetMapping
    public List<CategoryResponse> getAllCategories() {
        log.info("REST request to get all categories");
        List<CategoryResponse> categories = categoryService.getAllCategories();
        log.info("Returning {} categories", categories.size());
        return categories;
    }

    @Operation(summary = "Get category by ID",
            description = "Returns a category with the specified UUID")
    @GetMapping("/{id}")
    public CategoryResponse getCategoryById(@PathVariable UUID id) {
        log.info("REST request to get category with ID: {}", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        log.info("Category retrieved successfully: {}", id);
        return response;
    }

    @Operation(summary = "Get root categories",
            description = "Returns all categories without a parent category")
    @GetMapping("/root")
    public List<CategoryResponse> getRootCategories() {
        log.info("REST request to get root categories");
        List<CategoryResponse> categories = categoryService.getRootCategories();
        log.info("Returning {} root categories", categories.size());
        return categories;
    }

    @Operation(summary = "Get subcategories",
            description = "Returns all direct subcategories of the specified category")
    @GetMapping("/{parentId}/subcategories")
    public List<CategoryResponse> getSubcategories(@PathVariable UUID parentId) {
        log.info("REST request to get subcategories for category: {}", parentId);
        List<CategoryResponse> categories = categoryService.getSubcategories(parentId);
        log.info("Returning {} subcategories", categories.size());
        return categories;
    }


    @Operation(summary = "Update category",
            description = "Updates the name and parent category of an existing category")
    @PutMapping("/{id}")
    public CategoryResponse updateCategory(@PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        log.info("REST request to update category: {}", id);
        CategoryResponse response = categoryService.updateCategory(id, request.name(), request.parentId());
        log.info("Category updated successfully: {}", response.id());
        return response;
    }

    @Operation(summary = "Delete category",
            description = "Deletes a category that has no subcategories")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable UUID id) {
        log.info("REST request to delete category: {}", id);
        categoryService.deleteCategory(id);
        log.info("Category deleted successfully: {}", id);
    }
}
