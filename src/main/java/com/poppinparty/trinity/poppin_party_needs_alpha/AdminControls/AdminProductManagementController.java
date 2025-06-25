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
import org.springframework.ui.Model;


import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedProduct;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Category;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.CategoryRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;




@Controller
public class AdminProductManagementController {

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
