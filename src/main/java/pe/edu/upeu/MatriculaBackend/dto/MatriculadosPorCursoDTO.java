package pe.edu.upeu.MatriculaBackend.dto;

import java.math.BigDecimal;

public record MatriculadosPorCursoDTO(
        String codigo,
        String curso,
        Long matriculados,
        BigDecimal montoRecaudado
) {
}
