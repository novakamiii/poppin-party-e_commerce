/**
 * WebConfig is a Spring configuration class that customizes the handling of static resources.
 * <p>
 * This configuration maps all requests with the pattern "/uploads/**" to the local file system directory "uploads/".
 * Files placed in the "uploads" directory will be accessible via HTTP requests to "/uploads/{filename}".
 * </p>
 *
 * Implements {@link WebMvcConfigurer} to override the {@code addResourceHandlers} method.
 */
package com.poppinparty.trinity.poppin_party_needs_alpha.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
          .addResourceHandler("/uploads/**")
          .addResourceLocations("file:uploads/");
    }
}

