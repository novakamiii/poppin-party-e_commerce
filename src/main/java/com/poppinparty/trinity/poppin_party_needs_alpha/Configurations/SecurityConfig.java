package com.poppinparty.trinity.poppin_party_needs_alpha.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig is a Spring Security configuration class that sets up authentication and authorization
 * for the application. It defines beans for user details service, password encoding, and authentication provider.
 * The security filter chain configures HTTP security, specifying which endpoints are publicly accessible,
 * which require specific roles, and which require authentication. It also sets up form-based login and logout,
 * disables CSRF protection, and uses BCrypt for password encoding.
 *
 * Key Features:
 * <ul>
 *   <li>Defines public endpoints (e.g., registration, login, static resources).</li>
 *   <li>Restricts access to "/admin/**" for users with the ADMIN role and "/user/**" for users with the USER role.</li>
 *   <li>Configures form login with a custom login page and success redirect.</li>
 *   <li>Configures logout to redirect to the home page.</li>
 *   <li>Disables CSRF protection for simplicity (not recommended for production).</li>
 *   <li>Uses BCryptPasswordEncoder with strength 12 for secure password hashing.</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

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
                                "/css/**", "/js/**", "/api/products/**", "/uploads/**", "/product-page/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()
                ) // <-- Close the lambda for authorizeHttpRequests here
                //god forbid how this works
                //huh pano to
                //what
                //wag mo na galawin (coping)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/redirect", true) // 👈 redirect based on role
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/home")
                        .permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

}
