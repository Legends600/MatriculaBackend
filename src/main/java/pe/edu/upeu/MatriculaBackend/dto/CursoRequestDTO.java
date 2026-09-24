package pe.edu.upeu.MatriculaBackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CursoRequestDTO(
        @NotNull(message = "El código es obligatorio")
        @Pattern(regexp = "^[A-Z]{2}\\d{3}$", message = "El código debe tener el patrón AA999")
        String codigo,

        @NotNull(message = "El nombre es obligatorio")
        @Size(min = 3, max = 150, message = "El nombre debe tener entre 3 y 150 caracteres")
        String nombre,

        @NotNull(message = "Los créditos son obligatorios")
        @Min(value = 1, message = "Los créditos deben ser como mínimo 1")
        @Max(value = 6, message = "Los créditos deben ser como máximo 6")
        Integer creditos,

        @NotNull(message = "El ciclo es obligatorio")
        @Min(value = 1, message = "El ciclo debe ser como mínimo 1")
        @Max(value = 10, message = "El ciclo debe ser como máximo 10")
        Integer ciclo,

        @NotNull(message = "Las vacantes son obligatorias")
        @Min(value = 0, message = "Las vacantes no pueden ser negativas")
        Integer vacantes,

        @NotNull(message = "El estado es obligatorio")
        Boolean estado,

        @NotNull(message = "La carrera es obligatoria")
        Long carreraId
) {
}
