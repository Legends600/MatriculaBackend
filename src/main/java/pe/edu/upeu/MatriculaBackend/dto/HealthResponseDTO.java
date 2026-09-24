package pe.edu.upeu.MatriculaBackend.dto;

import java.time.LocalDateTime;

public record HealthResponseDTO(
        String status,
        String database,
        LocalDateTime timestamp
) {
}
