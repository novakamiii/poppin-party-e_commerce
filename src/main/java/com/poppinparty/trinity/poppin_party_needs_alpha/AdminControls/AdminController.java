package com.poppinparty.trinity.poppin_party_needs_alpha.AdminControls;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrderItems;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedProduct;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Category;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Product;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedUsers;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedPayments;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Order;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.OrderItem;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.Payment;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.ArchivedOrders;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.CategoryRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

import jakarta.transaction.Transactional;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedUserRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedOrderItemsRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedPaymentsRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ArchivedOrdersRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class AdminController {
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ArchivedProductRepository archivedProductRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArchivedUserRepository archivedUserRepository;

    @Autowired
    private ArchivedOrderItemsRepository archivedOrderItemsRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ArchivedPaymentsRepository archivedPaymentsRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ArchivedOrdersRepository archivedOrdersRepository;

    @Autowired
    private PaymentRepository paymentRepository;

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

    // ========== ACCOUNT ADMINS MANAGEMENT ==========

    @GetMapping("/admin/accountManagement")
    public String showAccounts(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin_account_management";
    }

    @GetMapping("/admin/user/restore")
    public String showArchivedUsers(Model model) {
        List<ArchivedUsers> archivedUsers = archivedUserRepository.findAll();
        model.addAttribute("archivedUsers", archivedUsers);
        return "accounts_archived";
    }

    @Transactional
    @GetMapping("/admin/user/delete/{id}")
    public String archiveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id: " + id));

        // Step 1: Archive User
        ArchivedUsers archivedUser = new ArchivedUsers();
        archivedUser.setName(user.getName());
        archivedUser.setPhone(user.getPhone());
        archivedUser.setUsername(user.getUsername());
        archivedUser.setPassword(user.getPassword());
        archivedUser.setAddress(user.getAddress());
        archivedUser.setEmail(user.getEmail());
        archivedUser.setRole(user.getRole());
        archivedUser.setImagePath(user.getImagePath());
        archivedUser.setGender(user.getGender());
        archivedUser.setBirthDate(user.getBirthDate());
        archivedUser.setLastLogin(user.getLastLogin());
        archivedUser.setOriginalUserId(user.getId());
        archivedUserRepository.saveAndFlush(archivedUser); // make sure it has an ID

        // Step 2: Archive Orders first (used for mapping to payments)
        List<Order> orders = orderRepository.findByUserId(id);
        Map<Long, ArchivedOrders> archivedOrderMap = new HashMap<>();
        for (Order order : orders) {
            ArchivedOrders archivedOrder = new ArchivedOrders();
            archivedOrder.setOriginalOrderId(order.getId());
            archivedOrder.setUserId(order.getUserId());
            archivedOrder.setOrderDate(order.getOrderDate());
            archivedOrder.setTotalAmount(order.getTotalAmount());
            archivedOrder.setStatus(order.getStatus());
            archivedOrder.setPaymentMethod(order.getPaymentMethod());
            archivedOrder.setShippingAddress(order.getShippingAddress());
            archivedOrder.setTrackingNumber(order.getTrackingNumber());
            archivedOrder.setShippingOption(order.getShippingOption());
            archivedOrdersRepository.save(archivedOrder);

            archivedOrderMap.put(order.getId(), archivedOrder);
        }

        // Step 3: Archive Payments (before deleting orders)
        List<Payment> payments = paymentRepository.findByUserId(id);
        for (Payment payment : payments) {
            ArchivedPayments archivedPayment = new ArchivedPayments();
            archivedPayment.setUserId(archivedUser.getId()); // ✅ Use the archivedUser ID!

            ArchivedOrders relatedArchivedOrder = archivedOrderMap.get(
                    payment.getOrder() != null ? payment.getOrder().getId() : null);
            archivedPayment.setOrder(relatedArchivedOrder);

            archivedPayment.setProductId(payment.getProductId());
            archivedPayment.setItemName(payment.getItemName());
            archivedPayment.setAmount(payment.getAmount());
            archivedPayment.setTransactionId(payment.getTransactionId());
            archivedPayment.setStatus(payment.getStatus());
            archivedPayment.setPaymentMethodDetails(payment.getPaymentMethodDetails());
            archivedPayment.setShippingOption(payment.getShippingOption());
            archivedPayment.setDaysLeft(payment.getDaysLeft());
            archivedPayment.setQuantity(payment.getQuantity());
            archivedPayment.setCustomProductRef(payment.getCustomProductRef());
            archivedPayment.setIsCustom(payment.getIsCustom());

            archivedPaymentsRepository.save(archivedPayment);
        }

        // Step 4: Archive Order Items
        List<OrderItem> orderItems = orderItemRepository.findByUserId(id);
        for (OrderItem item : orderItems) {
            ArchivedOrderItems archivedItem = new ArchivedOrderItems();
            archivedItem.setUserId(user.getId()); // original userId is correct here
            archivedItem.setProductRef(item.getProductRef());
            archivedItem.setQuantity(item.getQuantity());
            archivedItem.setUnitPrice(item.getUnitPrice());
            archivedItem.setCustom(item.isCustom());
            archivedItem.setCustomSize(item.getCustomSize());
            archivedItem.setEventType(item.getEventType());
            archivedItem.setPersonalizedMessage(item.getPersonalizedMessage());
            archivedItem.setTarpaulinThickness(item.getTarpaulinThickness());
            archivedItem.setTarpaulinFinish(item.getTarpaulinFinish());
            archivedOrderItemsRepository.save(archivedItem);
        }

        // Step 5: Delete in the correct order
        paymentRepository.deleteAll(payments);
        orderItemRepository.deleteAll(orderItems);
        orderRepository.deleteAll(orders);
        userRepository.deleteById(id);

        return "redirect:/admin/accountManagement";
    }

    @Transactional
    @PostMapping("/admin/user/restore/{id}")
    public String restoreUser(@PathVariable Long id) {
        // 1. Find archived user
        ArchivedUsers archivedUser = archivedUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Archived user not found: " + id));

        log.info("Starting restoration of user. Archived ID: {}, Original ID: {}", id,
                archivedUser.getOriginalUserId());

        // 2. Restore User
        User restoredUser = new User();
        restoredUser.setName(archivedUser.getName());
        restoredUser.setPhone(archivedUser.getPhone());
        restoredUser.setUsername(archivedUser.getUsername());
        restoredUser.setPassword(archivedUser.getPassword());
        restoredUser.setAddress(archivedUser.getAddress());
        restoredUser.setEmail(archivedUser.getEmail());
        restoredUser.setRole(archivedUser.getRole());
        restoredUser.setImagePath(archivedUser.getImagePath());
        restoredUser.setGender(archivedUser.getGender());
        restoredUser.setBirthDate(archivedUser.getBirthDate());
        restoredUser.setLastLogin(archivedUser.getLastLogin());

        User savedUser = userRepository.save(restoredUser);
        log.info("Restored user with new ID: {}", savedUser.getId());

        Long originalUserId = archivedUser.getOriginalUserId();

        // 3. Restore Orders
        List<ArchivedOrders> archivedOrders = archivedOrdersRepository.findByUserId(originalUserId);
        Map<Long, Order> orderIdMapping = new HashMap<>();
        log.info("Found {} orders to restore", archivedOrders.size());

        for (ArchivedOrders archivedOrder : archivedOrders) {
            Order order = new Order();
            order.setUserId(savedUser.getId());
            order.setOrderDate(archivedOrder.getOrderDate());
            order.setTotalAmount(archivedOrder.getTotalAmount());
            order.setStatus(archivedOrder.getStatus() != null ? archivedOrder.getStatus() : "PENDING");
            order.setPaymentMethod(archivedOrder.getPaymentMethod());
            order.setShippingAddress(archivedOrder.getShippingAddress());
            order.setTrackingNumber(archivedOrder.getTrackingNumber());
            order.setShippingOption(archivedOrder.getShippingOption());

            Order savedOrder = orderRepository.save(order);
            orderIdMapping.put(archivedOrder.getId(), savedOrder);
            log.debug("Restored order ID {} (original {})", savedOrder.getId(), archivedOrder.getId());
        }

        // 4. Restore Order Items
        List<ArchivedOrderItems> archivedItems = archivedOrderItemsRepository.findByUserId(originalUserId);
        log.info("Found {} order items to restore", archivedItems.size());

        for (ArchivedOrderItems archivedItem : archivedItems) {
            OrderItem item = new OrderItem();
            item.setUserId(savedUser.getId());
            item.setProductRef(archivedItem.getProductRef());
            item.setQuantity(archivedItem.getQuantity());
            item.setUnitPrice(archivedItem.getUnitPrice());
            item.setCustom(archivedItem.isCustom());
            item.setCustomSize(archivedItem.getCustomSize());
            item.setEventType(archivedItem.getEventType());
            item.setPersonalizedMessage(archivedItem.getPersonalizedMessage());
            item.setTarpaulinThickness(archivedItem.getTarpaulinThickness());
            item.setTarpaulinFinish(archivedItem.getTarpaulinFinish());

            if (archivedItem.getOrder() != null) {
                Order newOrder = orderIdMapping.get(archivedItem.getOrder().getId());
                if (newOrder != null) {
                    item.setOrder(newOrder);
                } else {
                    log.warn("Could not find restored order for item {}", archivedItem.getId());
                }
            }

            orderItemRepository.save(item);
        }

        // 5. Restore Payments - FIXED QUERY
        List<ArchivedPayments> archivedPayments = archivedPaymentsRepository.findByUserId(id); // Using archived user ID
        log.info("Found {} payments to restore for archived user ID {}", archivedPayments.size(), id);

        for (ArchivedPayments archivedPayment : archivedPayments) {
            Payment payment = new Payment();
            payment.setUser(savedUser);

            // Link to restored order if available
            if (archivedPayment.getOrder() != null) {
                Order newOrder = orderIdMapping.get(archivedPayment.getOrder().getId());
                if (newOrder != null) {
                    payment.setOrder(newOrder);
                    log.debug("Linked payment to order {}", newOrder.getId());
                } else {
                    log.warn("Could not find restored order for payment {}", archivedPayment.getId());
                }
            }

            // Copy all payment fields with null checks
            payment.setStatus(archivedPayment.getStatus() != null ? archivedPayment.getStatus() : "PENDING");
            payment.setProductId(archivedPayment.getProductId());
            payment.setItemName(archivedPayment.getItemName());
            payment.setAmount(archivedPayment.getAmount() != null ? archivedPayment.getAmount() : BigDecimal.ZERO);
            payment.setTransactionId(archivedPayment.getTransactionId());
            payment.setPaymentMethodDetails(archivedPayment.getPaymentMethodDetails());
            payment.setShippingOption(archivedPayment.getShippingOption());
            payment.setDaysLeft(archivedPayment.getDaysLeft());
            payment.setQuantity(archivedPayment.getQuantity() != null ? archivedPayment.getQuantity() : 0);
            payment.setCustomProductRef(archivedPayment.getCustomProductRef());
            payment.setIsCustom(archivedPayment.getIsCustom() != null ? archivedPayment.getIsCustom() : false);

            Payment savedPayment = paymentRepository.save(payment);
            log.debug("Restored payment ID {}", savedPayment.getId());
        }

        // 6. Cleanup archived data
        // 6. Cleanup archived data
        try {
            archivedPaymentsRepository.deleteAllByUserId(id);
            archivedOrderItemsRepository.deleteAllByUserId(originalUserId);
            archivedOrdersRepository.deleteAllByUserId(originalUserId);
            archivedUserRepository.deleteById(id);

            log.info("Cleanup completed for archived user ID {}", id);
        } catch (Exception e) {
            log.error("Error during cleanup: ", e);
            throw new RuntimeException("Cleanup failed", e);
        }

        return "redirect:/admin/accountManagement?restoreSuccess=true";
    }
}
