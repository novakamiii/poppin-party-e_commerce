package com.poppinparty.trinity.poppin_party_needs_alpha.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.poppinparty.trinity.poppin_party_needs_alpha.Repositories.UserRepository;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    public UserRepository userRepository;

    String getEmailByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public void sendResetCode(String toEmail, String code) {
        String username = getUsernameByEmail(toEmail);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Code");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f9f9f9; border-radius: 10px;'>"
                    +
                    "<h2 style='color: #b944fd;'>Hello, " + username + "!</h2>" +
                    "<p style='font-size: 16px;'>Here is your password reset code:</p>" +
                    "<div style='font-size: 24px; font-weight: bold; color: #333; background: #eee; padding: 10px 20px; display: inline-block; border-radius: 5px;'>"
                    + code + "</div>" +
                    "<p style='font-size: 14px; margin-top: 20px;'>If you didn’t request this, please ignore this email.</p>"
                    +
                    "<p style='font-size: 12px; color: #888;'>– Poppin Party Team</p>" +
                    "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    String getUsernameByEmail(String email) {
        var user = userRepository.findByEmail(email);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + email);
        }
        return user.getUsername();
    }
}
