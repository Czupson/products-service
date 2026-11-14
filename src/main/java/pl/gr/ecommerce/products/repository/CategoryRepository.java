package pl.gr.ecommerce.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.gr.ecommerce.products.category.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameIgnoreCase(String name);
    List<Category> findByParent_Id(UUID parentId);
    List<Category> findByParentIsNull();
}