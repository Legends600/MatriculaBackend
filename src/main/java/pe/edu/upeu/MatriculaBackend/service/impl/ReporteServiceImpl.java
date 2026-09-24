package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.upeu.MatriculaBackend.dto.MatriculadosPorCursoDTO;
import pe.edu.upeu.MatriculaBackend.repository.DetalleMatriculaRepository;
import pe.edu.upeu.MatriculaBackend.service.service.ReporteService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteServiceImpl implements ReporteService {

    private final DetalleMatriculaRepository detalleMatriculaRepository;

    @Override
    public List<MatriculadosPorCursoDTO> matriculadosPorCurso(String periodo, Long carreraId) {
        log.info("Generando reporte de matriculados por curso periodo={} carreraId={}", periodo, carreraId);
        return detalleMatriculaRepository.matriculadosPorCurso(periodo, carreraId);
    }
}
