package pl.gr.ecommerce.products.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCategoryRequest(
        @NotBlank(message = "Category name cannot be empty")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
        String name, UUID parentId
) {}
