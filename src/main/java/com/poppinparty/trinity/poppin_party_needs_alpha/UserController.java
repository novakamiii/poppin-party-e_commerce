package com.poppinparty.trinity.poppin_party_needs_alpha;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
// Removed incorrect import for java.util.Locale.Category
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import jakarta.persistence.Column;
import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @GetMapping("/dummy")
    public String dummyPage() {
        return "dummy";
    }

    // ========== LOGIN, REDIRECT, REGISTRATION ==========

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/checkAuth")
    public String checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            return "Authenticated as: " + authentication.getName() + ", Roles: " + authentication.getAuthorities();
        } else {
            return "Not authenticated";
        }
    }

    @GetMapping("/redirect")
    public String redirectBasedOnRole(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "redirect:/home";
        }

        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/home";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"))) {
            return "redirect:/user/home";
        }

        return "redirect:/home";
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        model.addAttribute("products", productRepository.findAll());
        updateLastLogin();
        return "product_list";
    }

    // Update last login time
    // why the fuck it is here.
    // idk nagana lang
    private void updateLastLogin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();

            userRepository.findByUsername(username).ifPresent(user -> {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
            });
        }
    }

    @GetMapping("/admin/page")
    public String adminPage() {
        updateLastLogin();
        return "index";
    }

    @GetMapping("/home")
    public String noLogin() {
        return "index";
    }

    @GetMapping("/user/home")
    public String userHome() {
        updateLastLogin();
        return "index";
    }

    @GetMapping("/")
    public String setHome() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("genders", User.Gender.values()); // Critical!
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        // FIXED: Match exact constraint values
        user.setRole("USER"); // Changed from "ROLE_USER"
        user.setLastLogin(LocalDateTime.now());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Registration complete!");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "No account associated with that email.");
            return "forgot_password";
        }

        return "redirect:/reset-password?email=" + email;
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String processReset(@RequestParam("email") String email,
            @RequestParam("newPassword") String newPassword) {
        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
            userRepository.save(user);
        }

        return "redirect:/login?resetSuccess";
    }

    // ========== USER MANAGEMENT ==========

    @GetMapping("/account")
    public String showDashboard(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new IllegalStateException("No user detected!");
        }

        // Fetch the latest user data
        User user = userRepository.findByUsername(customUserDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with username: " + customUserDetails.getUsername()));

        // Add the user to the model
        model.addAttribute("user", user);

        return "account";
    }

    @PostMapping("/account/update")
    public String updateAccount(@ModelAttribute("user") User formUser,
            BindingResult result,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Validation errors occurred.");
            return "redirect:/account";
        }

        // Find the currently logged-in user by username
        User existingUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(
                        () -> new IllegalArgumentException("User not found with username: " + principal.getName()));

        // Update fields — only update what’s editable
        existingUser.setGender(formUser.getGender());
        existingUser.setEmail(formUser.getEmail());
        existingUser.setName(formUser.getName());
        existingUser.setPhone(formUser.getPhone());

        // Save updated user
        userRepository.save(existingUser);

        // Add a success message
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        // Redirect to the GET /account endpoint to fetch updated data
        return "redirect:/account";
    }
    // ========== ITEMS ==========

    @GetMapping("/product-page")
    public String productPage() {
        return "product_page";
    }

    @RestController
    @RequestMapping("/api/products")
    public class ProductController {

        @Autowired
        private ProductRepository productRepository;

        private static final String UPLOAD_DIR = "uploads/";

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<Product> addProduct(
                @RequestParam("item_name") String itemName,
                @RequestParam("price") Double price,
                @RequestParam("stock") Long stock,
                @RequestParam(value = "category", required = false) String category,
                @RequestParam(value = "description", required = false) String description,
                @RequestParam("image") MultipartFile imageFile) throws IOException {

            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";

            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
            Path uploadPath = Paths.get(UPLOAD_DIR);

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

        // ✅ MOVE THIS INSIDE ProductController
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
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(productDTOs);
        }
    }
    // ========== PRODUCT MANAGEMENT ==========

    @GetMapping("/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "product_list";
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll(); // or any custom fetch logic
    }

    @GetMapping("/products/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product()); // For binding the form
        model.addAttribute("categories", getAllCategories()); // <-- This is crucial
        return "add_product"; // Name of your Thymeleaf HTML file
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
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageLoc("/uploads/" + filename); // For rendering via <img>
            } catch (IOException e) {
                e.printStackTrace();
                // Optional: Add error handling here
            }
        }

        product.setCreatedAt(LocalDateTime.now().toString());
        productRepository.save(product);

        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));

        List<com.poppinparty.trinity.poppin_party_needs_alpha.Category> categories = categoryRepository.findAll(); // You
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
            @PathVariable("id") Long id,
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
                Path uploadPath = Paths.get(uploadDir);
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(filename);
                Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                existingProduct.setImageLoc("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
                // Optional: add error handling here
            }
        }

        // You may want to preserve the original createdAt or update it depending on
        // your logic
        // existingProduct.setCreatedAt(existingProduct.getCreatedAt());

        productRepository.save(existingProduct);

        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products";
    }
}
