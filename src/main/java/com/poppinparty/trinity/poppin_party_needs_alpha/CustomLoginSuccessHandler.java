package com.poppinparty.trinity.poppin_party_needs_alpha;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {


    private final UserRepository userRepository;

    public CustomLoginSuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
                                        throws IOException, ServletException {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user != null) {
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);
        }

        // Redirect to the intended page
        response.sendRedirect("/redirect"); // or your homepage
    }
}