package com.nexcaja.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad de NexCaja.
 *
 * Spring Security bloquea TODOS los endpoints por defecto.
 * Esta clase le dice al framework qué rutas son públicas
 * y cuáles requieren autenticación.
 *
 * En esta fase (desarrollo) abrimos todos los endpoints /api/**
 * para que el frontend HTML pueda conectarse sin restricciones.
 * En producción se agregaría JWT para proteger las rutas sensibles.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactivar CSRF para permitir peticiones POST desde el frontend HTML
            // (CSRF se usa para formularios tradicionales, no para APIs REST)
            .csrf(AbstractHttpConfigurer::disable)

            // Configurar qué rutas son públicas y cuáles requieren login
            .authorizeHttpRequests(auth -> auth
                // Consola H2 (solo desarrollo)
                .requestMatchers("/h2-console/**").permitAll()
                // Todos los endpoints de la API son públicos en esta fase
                // En producción: .requestMatchers("/api/usuarios/login").permitAll()
                //                 .anyRequest().authenticated()
                .requestMatchers("/api/**").permitAll()
                // Cualquier otra ruta también es pública por ahora
                .anyRequest().permitAll()
            )

            // Necesario para que la consola H2 (que usa frames) funcione en el navegador
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            );

        return http.build();
    }
}
