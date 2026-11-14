package pl.gr.ecommerce.products.exception.category;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(UUID id) {
        super("Category not found with ID: " + id);
    }
}