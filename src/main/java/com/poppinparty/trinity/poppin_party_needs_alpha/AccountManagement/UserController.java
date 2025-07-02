package com.poppinparty.trinity.poppin_party_needs_alpha.AccountManagement;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import java.time.Duration;

import com.poppinparty.trinity.poppin_party_needs_alpha.Entities.User;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.ProductRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;
import com.poppinparty.trinity.poppin_party_needs_alpha.Services.EmailService;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

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

    @Autowired
    private EmailService emailService;

    // ========== LOGIN, REDIRECT, REGISTRATION ==========

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/checkAuth")
    @ResponseBody
    public String checkAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            return "Authenticated as: " + authentication.getName() + ", Roles: " + authentication.getAuthorities();
        } else {
            return "Not authenticated";
        }
    }

    @GetMapping("/api/auth/check")
    public ResponseEntity<Map<String, Object>> checkAuth(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            result.put("authenticated", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
        }
        result.put("authenticated", true);
        result.put("username", auth.getName());
        result.put("roles", auth.getAuthorities());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/authentication/check")
    public ResponseEntity<Map<String, Object>> checkAuth(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        if (principal == null) {
            response.put("authenticated", false);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        response.put("authenticated", true);
        return ResponseEntity.ok(response);
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
            return "redirect:/home";
        }

        return "redirect:/home";
    }

    @GetMapping("/admin/home")
    public String adminHome(Model model) {
        model.addAttribute("products", productRepository.findAll());
        updateLastLogin();
        return "admin-dashboard";
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

        user.setRole("USER");
        user.setLastLogin(LocalDateTime.now());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));

        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Registration complete!");
        return "redirect:/login";
    }

    @GetMapping("/api/check/email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/api/check/username")
    public ResponseEntity<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean exists = userRepository.existsByUsername(username);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/api/verify-reset-code")
    @ResponseBody
    public Map<String, Object> verifyResetCodeDebug(
            @RequestParam String email,
            @RequestParam String code,
            @CookieValue(value = "resetCode", required = false) String storedCode,
            @CookieValue(value = "resetEmail", required = false) String storedEmail) {

        System.out.println("Received: " + code + " for " + email);
        System.out.println("Stored code: " + storedCode + " | Stored email: " + storedEmail);

        boolean valid = storedCode != null && storedEmail != null &&
                storedEmail.equals(email) &&
                storedCode.equals(code);

        return Map.of(
                "valid", valid,
                "storedCode", storedCode,
                "storedEmail", storedEmail,
                "incomingCode", code,
                "incomingEmail", email);
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    @ResponseBody
    public ResponseEntity<?> processForgotPassword(@RequestParam String email, HttpSession session) {
        User user = userRepository.findByEmail(email);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No account associated with that email."));
        }

        String code = String.format("%06d", new Random().nextInt(999999));

        // Set secure cookies instead of relying on session (which is unstable with
        // fetch)
        ResponseCookie codeCookie = ResponseCookie.from("resetCode", code)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();

        ResponseCookie emailCookie = ResponseCookie.from("resetEmail", email)
                .httpOnly(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .build();

        System.out.println("Reset code for " + email + ": " + code);

        try {
            emailService.sendResetCode(email, code);
        } catch (Exception e) {
            System.out.println("Email send failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to send email."));
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, codeCookie.toString())
                .header(HttpHeaders.SET_COOKIE, emailCookie.toString())
                .body(Map.of("redirectUrl", "/reset-password?email=" + email));
    }

    @GetMapping("/reset-password")
    public String showResetForm(@RequestParam String email, Model model) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            model.addAttribute("email", email);
            model.addAttribute("username", "Unknown User");
        } else {
            model.addAttribute("email", email);
            model.addAttribute("username", user.getUsername());
        }
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> processReset(
            @RequestParam String email,
            @RequestParam String newPassword,
            @RequestParam String resetCode,
            @CookieValue(value = "resetCode", required = false) String storedCode,
            @CookieValue(value = "resetEmail", required = false) String storedEmail,
            HttpSession session,
            Model model) {

        if (!email.equals(storedEmail) || !resetCode.equals(storedCode)) {
            model.addAttribute("error", "Invalid reset code or session expired.");
            model.addAttribute("email", email);
            model.addAttribute("username", "Unknown");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid reset attempt");
        }

        User user = userRepository.findByEmail(email);
        if (user != null) {
            user.setPassword(new BCryptPasswordEncoder().encode(newPassword));
            userRepository.save(user);
        }

        ResponseCookie clearCode = ResponseCookie.from("resetCode", "")
                .httpOnly(true).path("/").maxAge(0).build();
        ResponseCookie clearEmail = ResponseCookie.from("resetEmail", "")
                .httpOnly(true).path("/").maxAge(0).build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, clearCode.toString())
                .header(HttpHeaders.SET_COOKIE, clearEmail.toString())
                .header("Location", "/login?resetFromEmail=true")
                .build();
    }
}