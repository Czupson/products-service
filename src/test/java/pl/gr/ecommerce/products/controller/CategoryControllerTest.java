package pl.gr.ecommerce.products.controller;

import pl.gr.ecommerce.products.dto.category.UpdateCategoryRequest;
import pl.gr.ecommerce.products.exception.category.CategoryNotFoundException;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import pl.gr.ecommerce.products.dto.category.CategoryResponse;
import pl.gr.ecommerce.products.dto.category.CreateCategoryRequest;
import pl.gr.ecommerce.products.service.CategoryService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void shouldCreateCategory() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        CreateCategoryRequest request = new CreateCategoryRequest("Elektronika", null);
        CategoryResponse response = new CategoryResponse(
                categoryId,
                "Elektronika",
                null,
                List.of()
        );
        when(categoryService.createCategory("Elektronika", null)).thenReturn(response);
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Elektronika"));
    }

    @Test
    void shouldGetAllCategories() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryResponse category = new CategoryResponse(
                categoryId,
                "Elektronika",
                null,
                List.of()
        );
        when(categoryService.getAllCategories()).thenReturn(List.of(category));
        // When & Then
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Elektronika"));
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryResponse response = new CategoryResponse(
                categoryId,
                "Elektronika",
                null,
                List.of()
        );
        when(categoryService.getCategoryById(categoryId)).thenReturn(response);
        // When & Then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Elektronika"));
    }


    @Test
    void shouldGetRootCategories() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        CategoryResponse category = new CategoryResponse(
                categoryId,
                "Elektronika",
                null,
                List.of()
        );
        when(categoryService.getRootCategories()).thenReturn(List.of(category));
        // When & Then
        mockMvc.perform(get("/api/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Elektronika"))
                .andExpect(jsonPath("$[0].parentId").doesNotExist());
    }

    @Test
    void shouldGetSubcategories() throws Exception {
        // Given
        UUID parentId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();
        CategoryResponse subcategory = new CategoryResponse(
                subcategoryId,
                "Smartfony",
                parentId,
                List.of()
        );
        when(categoryService.getSubcategories(parentId)).thenReturn(List.of(subcategory));
        // When & Then
        mockMvc.perform(get("/api/categories/{parentId}/subcategories", parentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(subcategoryId.toString()))
                .andExpect(jsonPath("$[0].name").value("Smartfony"))
                .andExpect(jsonPath("$[0].parentId").value(parentId.toString()));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Komputery", null);
        CategoryResponse response = new CategoryResponse(
                categoryId,
                "Komputery",
                null,
                List.of()
        );
        when(categoryService.updateCategory(categoryId, "Komputery", null))
                .thenReturn(response);
        // When & Then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId.toString()))
                .andExpect(jsonPath("$.name").value("Komputery"));
    }

    @Test
    void shouldDeleteCategory() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        doNothing().when(categoryService).deleteCategory(categoryId);
        // When & Then
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNoContent());
        verify(categoryService).deleteCategory(categoryId);
    }


    @Test
    void shouldReturnBadRequestWhenCategoryNameIsBlank() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest("", null);
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnBadRequestWhenCategoryNameExceedsMaxLength() throws Exception {
        // Given
        CreateCategoryRequest request = new CreateCategoryRequest(
                "A".repeat(101),
                null);
        // When & Then
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnNotFoundWhenCategoryDoesNotExist() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        when(categoryService.getCategoryById(categoryId)).thenThrow(new CategoryNotFoundException(categoryId));
        // When & Then
        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void shouldReturnNotFoundWhenParentCategoryDoesNotExist() throws Exception {
        // Given
        UUID parentId = UUID.randomUUID();
        when(categoryService.getSubcategories(parentId)).thenThrow(new CategoryNotFoundException(parentId));
        // When & Then
        mockMvc.perform(get("/api/categories/{parentId}/subcategories", parentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingCategoryWithBlankName() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("", null);
        // When & Then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnBadRequestWhenUpdatingCategoryNameExceedsMaxLength() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("A".repeat(101), null);
        // When & Then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnBadRequestWhenCategoryIdIsInvalid() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/categories/{id}", "invalid-uuid"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(categoryService);
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingCategoryDoesNotExist() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        UpdateCategoryRequest request = new UpdateCategoryRequest("Komputery", null);
        when(categoryService.updateCategory(categoryId, "Komputery", null)).thenThrow(new CategoryNotFoundException(categoryId));
        // When & Then
        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }


    @Test
    void shouldReturnNotFoundWhenDeletingCategoryDoesNotExist() throws Exception {
        // Given
        UUID categoryId = UUID.randomUUID();
        doThrow(new CategoryNotFoundException(categoryId))
                .when(categoryService)
                .deleteCategory(categoryId);
        // When & Then
        mockMvc.perform(delete("/api/categories/{id}", categoryId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
        verify(categoryService).deleteCategory(categoryId);
    }
}
