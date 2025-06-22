package com.poppinparty.trinity.poppin_party_needs_alpha.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    //this doesnt do anything
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

    //do you really need this?
    //what the hell even is this?
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
                                "/css/**", "/js/**", "/api/products/**", "/uploads/**", "/product-page/**", "/api/cart/**")
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
