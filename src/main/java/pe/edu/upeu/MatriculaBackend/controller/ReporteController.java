package pe.edu.upeu.MatriculaBackend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.MatriculaBackend.dto.MatriculadosPorCursoDTO;
import pe.edu.upeu.MatriculaBackend.service.service.ReporteService;

import java.util.List;

@Tag(name = "Reportes")
@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/matriculados-por-curso")
    public ResponseEntity<List<MatriculadosPorCursoDTO>> matriculadosPorCurso(
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) Long carreraId) {
        return ResponseEntity.ok(reporteService.matriculadosPorCurso(periodo, carreraId));
    }
}
