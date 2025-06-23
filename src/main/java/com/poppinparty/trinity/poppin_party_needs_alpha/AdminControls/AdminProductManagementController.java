/**
 * Controller for managing products in the admin panel.
 * <p>
 * Provides endpoints for listing, adding, editing, archiving, restoring, and deleting products.
 * Handles image uploads and category management (including creation of new categories).
 * Also supports archiving deleted products and restoring them from the archive.
 * </p>
 *
 * Endpoints:
 * <ul>
 *   <li><b>GET /products</b>: List all products.</li>
 *   <li><b>GET /products/add</b>: Show form to add a new product.</li>
 *   <li><b>POST /products/add</b>: Add a new product (with optional image and category creation).</li>
 *   <li><b>GET /products/edit/{id}</b>: Show form to edit an existing product.</li>
 *   <li><b>POST /products/update/{id}</b>: Update an existing product (with optional image and category creation).</li>
 *   <li><b>GET /products/delete/{id}</b>: Archive (soft-delete) a product.</li>
 *   <li><b>GET /products/archived</b>: List all archived products.</li>
 *   <li><b>GET /products/restore/{id}</b>: Restore an archived product to the main product list.</li>
 * </ul>
 *
 * <b>Image Upload:</b>
 * <ul>
 *   <li>Handles image uploads for products, storing them in the "uploads/" directory with a unique filename.</li>
 *   <li>Stores the image path in the product entity for rendering in views.</li>
 * </ul>
 *
 * <b>Category Management:</b>
 * <ul>
 *   <li>Allows selection of existing categories or creation of new categories when adding or editing products.</li>
 * </ul>
 *
 * <b>Archiving:</b>
 * <ul>
 *   <li>When a product is deleted, it is moved to the archive table instead of being permanently deleted.</li>
 *   <li>Archived products can be restored, but will receive a new ID due to JPA/Hibernate constraints.</li>
 * </ul>
 *
 * <b>Dependencies:</b>
 * <ul>
 *   <li>{@link ProductRepository} for product CRUD operations.</li>
 *   <li>{@link CategoryRepository} for category management.</li>
 *   <li>{@link ArchivedProductRepository} for managing archived products.</li>
 * </ul>
 *
 * <b>Note:</b>
 * <ul>
 *   <li>Uses Thymeleaf templates for rendering views (e.g., "product_list", "add_product", "edit_product", "products_archived").</li>
 *   <li>Logs actions using SLF4J Logger.</li>
 * </ul>
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.AdminControls;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;


import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedProduct;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Category;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.CategoryRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;




@Controller
public class AdminProductManagementController {
    private static final Logger log = LoggerFactory.getLogger(AdminProductManagementController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArchivedProductRepository archivedProductRepository;

    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "product_list";
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product()); // For binding the form
        model.addAttribute("categories", getAllCategories());
        return "add_product";
    }

    @PostMapping("/products/add")
    public String addProduct(
            @ModelAttribute Product product,
            @RequestParam(value = "category", required = false) String categoryId,
            @RequestParam(value = "newCategory", required = false) String newCategoryName,
            @RequestParam("image") MultipartFile imageFile) {

        Category category = null;

        if ("other".equals(categoryId)) {
            if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
                category = categoryRepository.findByName(newCategoryName).orElse(null);
                if (category == null) {
                    category = new Category();
                    category.setName(newCategoryName);
                    categoryRepository.save(category);
                }
            }
        } else {
            Long catId = Long.parseLong(categoryId);
            category = categoryRepository.findById(catId).orElse(null);
        }

        product.setCategory(category != null ? category.getName() : null);

        // ✅ Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Path.of(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageLoc("/uploads/" + filename); // For rendering via <img>
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        product.setCreatedAt(LocalDateTime.now().toString());
        productRepository.save(product);

        return "redirect:/products";
    }

    @GetMapping("/products/archived")
    public String showArchivedProducts(Model model) {
        List<ArchivedProduct> archivedProducts = archivedProductRepository.findAll();
        model.addAttribute("archivedProducts", archivedProducts);
        return "products_archived";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        List<com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Category> categories = categoryRepository
                .findAll(); // You
        // need
        // to
        // inject
        // categoryRepository

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        return "edit_product";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute Product updatedProduct,
            @RequestParam(value = "category", required = false) String categoryId,
            @RequestParam(value = "newCategory", required = false) String newCategoryName,
            @RequestParam(value = "image", required = false) MultipartFile imageFile) {

        // Load existing product from DB
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Update fields from the form
        existingProduct.setItemName(updatedProduct.getItemName());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        existingProduct.setDescription(updatedProduct.getDescription());

        // Handle category selection/new category
        Category category = null;
        if ("other".equals(categoryId)) {
            if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
                category = categoryRepository.findByName(newCategoryName).orElse(null);
                if (category == null) {
                    category = new Category();
                    category.setName(newCategoryName);
                    categoryRepository.save(category);
                }
            }
        } else if (categoryId != null && !categoryId.isEmpty()) {
            Long catId = Long.parseLong(categoryId);
            category = categoryRepository.findById(catId).orElse(null);
        }
        existingProduct.setCategory(category != null ? category.getName() : null);

        // Handle image upload if new image is provided
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String uploadDir = "uploads/";
                String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                Path uploadPath = Path.of(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                existingProduct.setImageLoc("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();

            }
        }

        // You may want to preserve the original createdAt or update it depending on
        // your logic
        // existingProduct.setCreatedAt(existingProduct.getCreatedAt());

        productRepository.save(existingProduct);

        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String archiveProduct(@PathVariable Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        // Copy to archive
        ArchivedProduct archived = new ArchivedProduct();
        archived.setId(product.getId());
        archived.setItemName(product.getItemName());
        archived.setPrice(product.getPrice());
        archived.setStock(product.getStock());
        archived.setDescription(product.getDescription());
        archived.setImageLoc(product.getImageLoc());
        archived.setCategory(product.getCategory());
        archived.setCreatedAt(product.getCreatedAt());
        archived.setArchivedAt(LocalDateTime.now().toString());

        archivedProductRepository.save(archived);
        productRepository.deleteById(id); // Then delete from main table

        return "redirect:/products";
    }

    @GetMapping("/products/restore/{id}")
    public String restoreProduct(@PathVariable Long id) {
        ArchivedProduct archived = archivedProductRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archived product not found: " + id));

        Product restored = new Product();
        // Make a workaround of this
        // hibernate does not allow to set id of its original id
        // why the fuck
        // I reached a conclusion that it is not possible to set id of an archived
        // product
        // let JPA handle it
        // restored.setId(archived.getId());
        restored.setItemName(archived.getItemName());
        restored.setPrice(archived.getPrice());
        restored.setStock(archived.getStock());
        restored.setDescription(archived.getDescription());
        restored.setImageLoc(archived.getImageLoc());
        restored.setCategory(archived.getCategory());
        restored.setCreatedAt(archived.getCreatedAt());

        productRepository.save(restored);
        archivedProductRepository.deleteById(id); // Remove from archive

        return "redirect:/products";
    }
}
