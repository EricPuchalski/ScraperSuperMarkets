package com_supermarket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Permitir todas las rutas
                        .allowedOrigins(
                                "http://localhost:4200, https://scraper-front.onrender.com/, https://super-posadas.netlify.app/"
                        ) // Agregar el dominio de ngrok
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permitir métodos HTTP específicos
                        .allowedHeaders("*") // Permitir todos los encabezados
                        .allowCredentials(true); // Permitir credenciales
            }
        };
    }
}