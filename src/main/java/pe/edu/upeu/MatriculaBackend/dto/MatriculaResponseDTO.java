package pe.edu.upeu.MatriculaBackend.dto;

import pe.edu.upeu.MatriculaBackend.enums.EstadoMatricula;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record MatriculaResponseDTO(
        Long id,
        LocalDateTime fecha,
        String periodo,
        Long estudianteId,
        String estudianteCodigo,
        String estudianteNombres,
        EstadoMatricula estado,
        Integer totalCreditos,
        BigDecimal montoTotal,
        List<DetalleMatriculaResponseDTO> detalles
) {
}
