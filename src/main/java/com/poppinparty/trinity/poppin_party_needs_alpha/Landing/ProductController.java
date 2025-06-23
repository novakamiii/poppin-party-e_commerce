/**
 * Controller for handling product-related web and API requests.
 * <p>
 * This controller provides endpoints for:
 * <ul>
 *   <li>Viewing products by category</li>
 *   <li>Rendering product-related pages (see more, custom tarp, product page)</li>
 *   <li>REST API for managing products (add, list, get by ID)</li>
 * </ul>
 * 
 * <h2>Web Endpoints:</h2>
 * <ul>
 *   <li><b>/products/category</b> - View products filtered by category</li>
 *   <li><b>/products/see-more</b> - Render the "see more" page</li>
 *   <li><b>/products/customtarp</b> - Render the custom tarp order page</li>
 *   <li><b>/product-page</b> - Render the general product page</li>
 *   <li><b>/product-page/{id}</b> - Render a specific product's page</li>
 * </ul>
 * 
 * <h2>REST API Endpoints (under /api/products):</h2>
 * <ul>
 *   <li><b>POST /api/products</b> - Add a new product with image upload (multipart/form-data)</li>
 *   <li><b>GET /api/products</b> - Retrieve all products as DTOs</li>
 *   <li><b>GET /api/products/{id}</b> - Retrieve a product by its ID</li>
 * </ul>
 * 
 * <p>
 * Image uploads are stored in the <code>uploads/</code> directory with a unique filename.
 * </p>
 * 
 * <p>
 * Uses {@link ProductRepository} for data access and supports both HTML view rendering and RESTful API responses.
 * </p>
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.Landing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ProductDTO;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;

@Controller
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // ========== ITEMS ==========
    @GetMapping("/products/category")
    public String viewByCategory(@RequestParam String name, Model model) {
        List<Product> products = productRepository.findByCategory(name);
        model.addAttribute("products", products);
        model.addAttribute("category", name);
        return "PerCategory"; // your view template
    }

    @GetMapping("/products/see-more")
    public String seeMore() {
        return "seemore"; // Ensure the view name matches your template
    }

    @GetMapping("/products/customtarp")
    public String orderBanner() {
        return "customtarp";
    }

    @GetMapping("/product-page")
    public String productPage() {
        return "product_page";
    }

    @RestController
    @RequestMapping("/api/products")
    public class ProductApiController {

        @Autowired
        private ProductRepository productRepository;

        private static final String UPLOAD_DIR = "uploads/";

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Product> addProduct(
                @RequestParam("item_name") String itemName,
                @RequestParam Double price,
                @RequestParam Long stock,
                @RequestParam(required = false) String category,
                @RequestParam(required = false) String description,
                @RequestParam("image") MultipartFile imageFile) throws IOException {

            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path uploadPath = Path.of(UPLOAD_DIR);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Product newProduct = new Product();
            newProduct.setItemName(itemName);
            newProduct.setPrice(price);
            newProduct.setStock(stock);
            newProduct.setCategory(category);
            newProduct.setDescription(description);
            newProduct.setImageLoc("/" + UPLOAD_DIR + uniqueFileName);

            productRepository.save(newProduct);

            return ResponseEntity.ok(newProduct);
        }

        @GetMapping
        public ResponseEntity<List<ProductDTO>> getAllProducts() {
            List<Product> products = productRepository.findAll();

            List<ProductDTO> productDTOs = products.stream().map(product -> {
                ProductDTO dto = new ProductDTO();
                dto.setId(product.getId());
                dto.setItemName(product.getItemName());
                dto.setPrice(product.getPrice());
                dto.setImageLoc(product.getImageLoc());
                dto.setStock(product.getStock());
                dto.setCategory(product.getCategory());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        }

        // ✅ FIXED: THIS METHOD SHOULD BE HERE INSIDE ProductController
        @GetMapping("/{id}")
        public ResponseEntity<Product> getProductById(@PathVariable Long id) {
            Optional<Product> product = productRepository.findById(id);
            return product.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        }
    }

    @GetMapping("/product-page/{id}")
    public String getProductPage(@PathVariable Long id, Model model) {
        return "product_page"; // renders product-page.html
    }
}
