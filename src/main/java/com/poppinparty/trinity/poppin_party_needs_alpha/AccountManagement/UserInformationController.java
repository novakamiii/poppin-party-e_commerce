/**
 * Controller for managing user account information and profile images.
 * <p>
 * Handles displaying the user dashboard, updating user profile details,
 * and uploading/deleting user profile images.
 * </p>
 *
 * <ul>
 *   <li>
 *     <b>GET /account</b>: Displays the account dashboard for the authenticated user.
 *   </li>
 *   <li>
 *     <b>POST /account/update</b>: Updates editable user profile fields (name, email, phone, gender, address).
 *   </li>
 *   <li>
 *     <b>POST /account/upload-image</b>: Handles profile image upload, deletes old image if not default,
 *     and updates the user's image path.
 *   </li>
 * </ul>
 *
 * <p>
 * Requires authentication for all endpoints. Uses {@link UserRepository} for persistence.
 * </p>
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.AccountManagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

@Controller
public class UserInformationController {

    @Autowired
    private UserRepository userRepository;

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

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/account/change-password")
    public String changePassword(@RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addAttribute("passwordError", "Passwords do not match.");
            return "redirect:/account";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addAttribute("passwordError", "Password must be at least 6 characters.");
            return "redirect:/account";
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // invalidate session and redirect to login
        SecurityContextHolder.clearContext(); // logout current session
        return "redirect:/login?passwordChanged=true";
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
