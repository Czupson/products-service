package pl.gr.ecommerce.products.dto.category;

import java.util.List;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        UUID parentId,
        List<CategoryResponse> children
) {}