package pe.edu.upeu.MatriculaBackend.dto;

import java.time.LocalDateTime;

public record EstudianteResponseDTO(
        Long id,
        String codigo,
        String dni,
        String nombres,
        String apellidos,
        String email,
        Boolean estado,
        Long carreraId,
        String carreraNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
