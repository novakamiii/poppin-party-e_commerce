package com.poppinparty.trinity.poppin_party_needs_alpha.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ThymeleafConfig implements WebMvcConfigurer {

    @Bean
    SpringSecurityDialect securityDialect() {
        return new SpringSecurityDialect();
    }
}
