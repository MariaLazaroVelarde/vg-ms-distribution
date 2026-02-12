package pe.edu.vallegrande.msdistribution.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de seguridad para el microservicio.
 * 
 * En desarrollo (perfil dev): Permite todo el tráfico sin autenticación.
 * En producción: Confía en los headers del API Gateway para autenticación.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    
    /**
     * Configuración de seguridad para el perfil de desarrollo.
     * Permite todo el tráfico sin restricciones.
     */
    @Bean
    @Profile("dev")
    public SecurityWebFilterChain securityWebFilterChainDev(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                )
                .build();
    }
    
    /**
     * Configuración de seguridad para producción.
     * Permite rutas públicas (actuator, swagger) y confía en el API Gateway
     * para la autenticación del resto de endpoints.
     */
    @Bean
    @Profile("!dev")
    public SecurityWebFilterChain securityWebFilterChainProd(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchanges -> exchanges
                        // Rutas públicas
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        .pathMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll()
                        .pathMatchers("/v3/api-docs/**", "/webjars/**").permitAll()
                        // El resto requiere autenticación vía Gateway
                        .anyExchange().permitAll() // Confiamos en el Gateway
                )
                .build();
    }
    
    /**
     * Configuración de CORS para permitir peticiones desde el frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
