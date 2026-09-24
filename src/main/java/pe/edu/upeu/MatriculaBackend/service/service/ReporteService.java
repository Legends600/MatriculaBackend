package pe.edu.upeu.MatriculaBackend.service.service;

import pe.edu.upeu.MatriculaBackend.dto.MatriculadosPorCursoDTO;

import java.util.List;

public interface ReporteService {

    List<MatriculadosPorCursoDTO> matriculadosPorCurso(String periodo, Long carreraId);
}
