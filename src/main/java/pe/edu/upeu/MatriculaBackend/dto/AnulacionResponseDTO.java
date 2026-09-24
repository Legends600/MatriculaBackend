package pe.edu.upeu.MatriculaBackend.dto;

import pe.edu.upeu.MatriculaBackend.enums.EstadoMatricula;

import java.util.List;

public record AnulacionResponseDTO(
        Long matriculaId,
        EstadoMatricula estado,
        List<CursoVacanteDTO> vacantes
) {
}
