package pe.edu.upeu.MatriculaBackend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EstudianteRequestDTO(
        @NotNull(message = "El código es obligatorio")
        @Pattern(regexp = "^\\d{9}$", message = "El código debe tener 9 dígitos")
        String codigo,

        @NotNull(message = "El DNI es obligatorio")
        @Pattern(regexp = "^\\d{8}$", message = "El DNI debe tener 8 dígitos")
        String dni,

        @NotNull(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
        String nombres,

        @NotNull(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
        String apellidos,

        @NotNull(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no debe superar 150 caracteres")
        String email,

        @NotNull(message = "El estado es obligatorio")
        Boolean estado,

        @NotNull(message = "La carrera es obligatoria")
        Long carreraId
) {
}
