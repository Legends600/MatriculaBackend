package pe.edu.upeu.MatriculaBackend.dto;

import jakarta.validation.constraints.NotNull;

public record DetalleMatriculaRequestDTO(
        @NotNull(message = "El curso es obligatorio")
        Long cursoId
) {
}
