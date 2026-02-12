package pe.edu.vallegrande.msdistribution.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Extrae los headers de autenticación enviados por el API Gateway.
 * El Gateway valida el JWT con Keycloak y reenvía la información en headers HTTP.
 */
@Slf4j
@Component
public class GatewayHeadersExtractor {
    
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_ROLES = "X-Roles";
    
    /**
     * Extrae la información del usuario autenticado desde los headers HTTP.
     * @param headers Headers HTTP de la petición
     * @return AuthenticatedUser con la información extraída
     */
    public AuthenticatedUser extract(HttpHeaders headers) {
        String userId = headers.getFirst(HEADER_USER_ID);
        String organizationId = headers.getFirst(HEADER_ORGANIZATION_ID);
        String email = headers.getFirst(HEADER_USER_EMAIL);
        String rolesHeader = headers.getFirst(HEADER_ROLES);
        
        List<String> roles = parseRoles(rolesHeader);
        
        log.debug("Extracted user from headers: userId={}, organizationId={}, email={}, roles={}", 
                  userId, organizationId, email, roles);
        
        return AuthenticatedUser.builder()
                .userId(userId)
                .organizationId(organizationId)
                .email(email)
                .roles(roles)
                .build();
    }
    
    /**
     * Parsea los roles desde el header X-Roles (separados por coma).
     * @param rolesHeader Valor del header X-Roles
     * @return Lista de roles
     */
    private List<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(rolesHeader.split(","));
    }
}
