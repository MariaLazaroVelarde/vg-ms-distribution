package pe.edu.vallegrande.msdistribution.application.events.program;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Evento publicado cuando se actualiza un programa de distribución.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistributionProgramUpdatedEvent {
    
    @Builder.Default
    private String eventType = "DISTRIBUTION_PROGRAM_UPDATED";
    
    private String eventId;
    private LocalDateTime timestamp;
    private String correlationId;
    
    // Datos del programa
    private String programId;
    private String organizationId;
    //private String programCode;
    
    // Campos que cambiaron
    private Map<String, Object> changedFields;
    
    // Auditoría
    private String updatedBy;
}
