package pe.edu.upeu.MatriculaBackend.dto;

import java.time.LocalDateTime;

public record CursoResponseDTO(
        Long id,
        String codigo,
        String nombre,
        Integer creditos,
        Integer ciclo,
        Integer vacantes,
        Boolean estado,
        Long carreraId,
        String carreraNombre,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaModificacion
) {
}
