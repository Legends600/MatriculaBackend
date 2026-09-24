package pe.edu.upeu.MatriculaBackend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CarreraRequestDTO(
        @NotNull(message = "El nombre es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
        String nombre,

        @Size(max = 200, message = "La descripción no debe superar 200 caracteres")
        String descripcion,

        @NotNull(message = "El estado es obligatorio")
        Boolean estado
) {
}
