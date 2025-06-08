package com.poppinparty.trinity.poppin_party_needs_alpha;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.persistence.Column;
import jakarta.validation.Valid;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

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
            user.setPassword(NoOpPasswordEncoder.getInstance().encode(newPassword));
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
    // ========== PRODUCT ==========

    @GetMapping("/products")
    public String showProducts(Model model) {
        model.addAttribute("products", productRepository.findAll());
        return "product_list";
    }

    @GetMapping("/products/add")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        return "add_product";
    }

    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product) {
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        model.addAttribute("product", product);
        return "edit_product";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable("id") Long id, Product product) {
        product.setId(id);
        productRepository.save(product);
        return "redirect:/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "redirect:/products";
    }
}
