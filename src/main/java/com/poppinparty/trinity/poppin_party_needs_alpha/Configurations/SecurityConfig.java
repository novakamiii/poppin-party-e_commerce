/**
 * SecurityConfig configures Spring Security for the application.
 * <p>
 * Main responsibilities:
 * <ul>
 *   <li>Defines beans for user details service, password encoder, and authentication provider.</li>
 *   <li>Configures HTTP security, including URL authorization, login, logout, and CSRF settings.</li>
 *   <li>Specifies which endpoints are publicly accessible and which require authentication or specific roles.</li>
 *   <li>Handles authentication exceptions by redirecting unauthorized users to the login page.</li>
 * </ul>
 *
 * Beans:
 * <ul>
 *   <li>{@link UserDetailsService} - Loads user-specific data.</li>
 *   <li>{@link PasswordEncoder} - Encodes passwords using BCrypt with strength 12.</li>
 *   <li>{@link DaoAuthenticationProvider} - Authenticates users using the custom user details service and password encoder.</li>
 *   <li>{@link SecurityFilterChain} - Configures the security filter chain for HTTP requests.</li>
 * </ul>
 *
 * URL Authorization:
 * <ul>
 *   <li>Public: /register, /dummy, /home, /styles.css, /img/**, /forgot-password, /reset-password, /login, /css/**, /js/**, /api/products/**, /uploads/**, /product-page/**, /api/cart/**, /products/see-more, /products/category/**</li>
 *   <li>Admin only: /admin/**</li>
 *   <li>User only: /user/**</li>
 *   <li>All other requests require authentication.</li>
 * </ul>
 *
 * Login/Logout:
 * <ul>
 *   <li>Custom login page at /login.</li>
 *   <li>Redirects to /redirect after successful login.</li>
 *   <li>Logout redirects to /home.</li>
 * </ul>
 *
 * CSRF protection is disabled.
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig {

    // this doesnt do anything
    private CustomLoginSuccessHandler loginSuccessHandler;

    @Bean
    UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        // Same encoder everywhere
        return new BCryptPasswordEncoder(12); // Strength 10-12 is safe
    }

    // do you really need this?
    // what the hell even is this?
    @Bean
    DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/register", "/dummy", "/home", "/styles.css", "/img/**",
                    "/forgot-password", "/reset-password", "/login",
                    "/css/**", "/js/**", "/api/products/**", "/uploads/**", "/product-page/**",
                    "/api/cart/**", "/products/see-more", "/products/category/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.sendRedirect("/login");
                })
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/redirect", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/home")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable());
        return http.build();
    }

}
