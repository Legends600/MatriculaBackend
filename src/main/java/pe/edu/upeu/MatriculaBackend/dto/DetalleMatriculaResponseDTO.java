package pe.edu.upeu.MatriculaBackend.dto;

import java.math.BigDecimal;

public record DetalleMatriculaResponseDTO(
        Long id,
        Long cursoId,
        String cursoCodigo,
        String cursoNombre,
        Integer creditos,
        BigDecimal costo
) {
}
