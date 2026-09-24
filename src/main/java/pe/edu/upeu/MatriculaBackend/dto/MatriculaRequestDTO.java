package pe.edu.upeu.MatriculaBackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record MatriculaRequestDTO(
        @NotNull(message = "El estudiante es obligatorio")
        Long estudianteId,

        @NotNull(message = "El periodo es obligatorio")
        @Pattern(regexp = "^\\d{4}-[12]$", message = "El periodo debe tener el patrón AAAA-1 o AAAA-2")
        String periodo,

        @NotEmpty(message = "Debe registrar al menos un curso")
        @Valid
        List<DetalleMatriculaRequestDTO> detalles
) {
}
