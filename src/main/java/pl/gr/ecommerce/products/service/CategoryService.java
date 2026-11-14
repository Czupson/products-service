package pl.gr.ecommerce.products.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.gr.ecommerce.products.category.Category;
import pl.gr.ecommerce.products.dto.category.CategoryResponse;
import pl.gr.ecommerce.products.exception.category.CategoryAlreadyExistsException;
import pl.gr.ecommerce.products.exception.category.CategoryNotFoundException;
import pl.gr.ecommerce.products.exception.category.InvalidCategoryException;
import pl.gr.ecommerce.products.mapper.CategoryMapper;
import pl.gr.ecommerce.products.repository.CategoryRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse createCategory(String name, UUID parentId) {
        log.info("Creating category: {}", name);
        if (name == null || name.isBlank()) {
            log.warn("Cannot create category: name is empty");
            throw new InvalidCategoryException("Category name cannot be empty");
        }
        String normalizedName = name.trim();
        if (categoryRepository.findByNameIgnoreCase(normalizedName).isPresent()) {
            log.warn("Cannot create category: category already exists: {}", normalizedName);
            throw new CategoryAlreadyExistsException(normalizedName);
        }
        Category category = new Category();
        category.setName(normalizedName);
        if (parentId != null) {
            log.debug("Looking for parent category with ID: {}", parentId);
            Category parent = getCategoryEntityById(parentId);
            category.setParent(parent);
        }
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());
        return categoryMapper.toResponse(savedCategory);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        log.info("Retrieved {} categories", categories.size());
        return categoryMapper.toResponseList(categories);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new CategoryNotFoundException(id);
                });
        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.debug("Fetching root categories");
        List<Category> categories = categoryRepository.findByParentIsNull();
        log.info("Retrieved {} root categories", categories.size());
        return categoryMapper.toResponseList(categories);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        log.debug("Fetching subcategories of parent category: {}", parentId);
        if (!categoryRepository.existsById(parentId)) {
            log.warn("Parent category not found with ID: {}", parentId);
            throw new CategoryNotFoundException(parentId);
        }
        List<Category> categories = categoryRepository.findByParent_Id(parentId);
        log.info("Retrieved {} subcategories for parent category: {}", categories.size(), parentId);
        return categoryMapper.toResponseList(categories);
    }


    public CategoryResponse updateCategory(UUID id, String name, UUID parentId) {
        log.info("Updating category with ID: {}", id);
        Category category = getCategoryEntityById(id);
        if (name == null || name.isBlank()) {
            log.warn("Cannot update category: name is empty");
            throw new InvalidCategoryException("Category name cannot be empty");
        }
        String normalizedName = name.trim();
        categoryRepository.findByNameIgnoreCase(normalizedName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    log.warn("Category name already exists: {}", normalizedName);
                    throw new CategoryAlreadyExistsException(normalizedName);
                });
        category.setName(normalizedName);
        validateParent(category, parentId);
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", id);
        return categoryMapper.toResponse(updatedCategory);
    }

    public void deleteCategory(UUID id) {
        log.info("Deleting category with ID: {}", id);
        Category category = getCategoryEntityById(id);
        if (!category.getChildren().isEmpty()) {
            log.warn("Cannot delete category {} because it has subcategories", id);
            throw new InvalidCategoryException(
                    "Cannot delete category with subcategories"
            );
        }
        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    private void validateParent(Category category, UUID parentId) {
        if (parentId == null) {
            category.setParent(null);
            return;
        }
        if (category.getId().equals(parentId)) {
            throw new InvalidCategoryException(
                    "Category cannot be its own parent"
            );
        }
        Category parent = getCategoryEntityById(parentId);
        Category current = parent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                throw new InvalidCategoryException(
                        "Category hierarchy cannot contain a cycle"
                );
            }
            current = current.getParent();
        }
        category.setParent(parent);
    }

    private Category getCategoryEntityById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new CategoryNotFoundException(id);
                });
    }
}
