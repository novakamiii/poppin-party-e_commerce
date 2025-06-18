package com.poppinparty.trinity.poppin_party_needs_alpha.AccountManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.CustomUserDetails;
import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.CategoryRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderItemRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.OrderRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.PaymentRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

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
        System.out.println("Redirecting based on role: " + authentication);

        if (authentication == null || authentication.getAuthorities().isEmpty()) {
            return "redirect:/home";
        }

        authentication.getAuthorities().forEach(auth -> System.out.println("Role: " + auth.getAuthority()));

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
        model.addAttribute("genders", User.Gender.values()); // Gender enum
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "register";
        }

        if (user.getImagePath() == null || user.getImagePath().isEmpty()) {
            user.setImagePath("/img/default-profile.png");
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
    public String processForgotPassword(@RequestParam String email, Model model) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            model.addAttribute("error", "No account associated with that email.");
            return "forgot_password";
        }

        return "redirect:/reset-password?email=" + email;
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String processReset(@RequestParam String email,
            @RequestParam String newPassword) {
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
        existingUser.setAddress(formUser.getAddress());
        existingUser.setName(formUser.getName());
        existingUser.setPhone(formUser.getPhone());

        // Save updated user
        userRepository.save(existingUser);

        // Add a success message
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");

        // Redirect to the GET /account endpoint to fetch updated data
        return "redirect:/account";
    }

    @PostMapping("/account/upload-image")
    @ResponseBody
    public Map<String, String> uploadProfileImage(@RequestParam("imageFile") MultipartFile file,
            Principal principal) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file uploaded");
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 🔥 Delete the old image file if it exists and isn't the default
        String oldImagePath = user.getImagePath();
        if (oldImagePath != null && !oldImagePath.equals("/img/default-profile.png")) {
            Path oldFile = Path.of("uploads", oldImagePath.replace("/uploads/", ""));
            if (Files.exists(oldFile) && Files.isRegularFile(oldFile)) { // ✅ ensures it's a file
                try {
                    Files.delete(oldFile);
                    System.out.println("Deleted old image: " + oldFile);
                } catch (IOException e) {
                    System.err.println("Failed to delete old image: " + e.getMessage());
                }
            }
        }

        // ✅ Save the new image
        String sanitizedUsername = user.getUsername().replaceAll("[^a-zA-Z0-9]", "");
        String uploadDir = "uploads/profiles/";
        String fileName = sanitizedUsername + "-" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        Path filePath = Path.of(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, file.getBytes());

        String newImagePath = "/uploads/profiles/" + fileName;
        user.setImagePath(newImagePath);
        userRepository.save(user);

        return Map.of("imageUrl", newImagePath);
    }
}