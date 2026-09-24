package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.dto.AnulacionResponseDTO;
import pe.edu.upeu.MatriculaBackend.dto.CursoVacanteDTO;
import pe.edu.upeu.MatriculaBackend.dto.DetalleMatriculaRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.DetalleMatriculaResponseDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Curso;
import pe.edu.upeu.MatriculaBackend.entity.DetalleMatricula;
import pe.edu.upeu.MatriculaBackend.entity.Estudiante;
import pe.edu.upeu.MatriculaBackend.entity.Matricula;
import pe.edu.upeu.MatriculaBackend.enums.EstadoMatricula;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.repository.CursoRepository;
import pe.edu.upeu.MatriculaBackend.repository.EstudianteRepository;
import pe.edu.upeu.MatriculaBackend.repository.MatriculaRepository;
import pe.edu.upeu.MatriculaBackend.service.service.MatriculaService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatriculaServiceImpl implements MatriculaService {

    private static final int MAX_CREDITOS = 20;

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;
    private final CursoRepository cursoRepository;

    @Value("${matricula.costo-credito}")
    private BigDecimal costoCredito;

    @Override
    @Transactional
    public MatriculaResponseDTO registrar(MatriculaRequestDTO request) {
        Estudiante estudiante = estudianteRepository.findById(request.estudianteId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado con id " + request.estudianteId()));

        if (!Boolean.TRUE.equals(estudiante.getEstado())) {
            log.warn("RN-01 violada: estudiante inactivo id={}", estudiante.getId());
            throw new ReglaNegocioException("Solo se puede matricular a un estudiante activo");
        }

        Set<Long> cursoIds = new LinkedHashSet<>();
        for (DetalleMatriculaRequestDTO detalle : request.detalles()) {
            if (!cursoIds.add(detalle.cursoId())) {
                log.warn("RN violada: curso duplicado id={} en la misma matrícula", detalle.cursoId());
                throw new ReglaNegocioException("No se puede matricular dos veces en el mismo curso");
            }
        }

        if (matriculaRepository.existsByEstudianteIdAndPeriodoAndEstado(
                estudiante.getId(), request.periodo(), EstadoMatricula.REGISTRADA)) {
            log.warn("RN-03 violada: estudiante id={} ya tiene matrícula registrada en periodo={}",
                    estudiante.getId(), request.periodo());
            throw new ReglaNegocioException("El estudiante ya tiene una matrícula registrada en ese periodo");
        }

        List<Curso> cursos = cursoIds.stream().map(id -> cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id " + id))).toList();

        int totalCreditos = 0;
        for (Curso curso : cursos) {
            if (!Boolean.TRUE.equals(curso.getEstado()) || !curso.getCarrera().getId().equals(estudiante.getCarrera().getId())) {
                log.warn("RN-01 violada: curso id={} inactivo o de otra carrera para estudiante id={}",
                        curso.getId(), estudiante.getId());
                throw new ReglaNegocioException("Solo se puede matricular en cursos activos de la propia carrera del estudiante");
            }
            if (curso.getVacantes() == null || curso.getVacantes() <= 0) {
                log.warn("RN-02 violada: curso id={} sin vacantes", curso.getId());
                throw new ReglaNegocioException("El curso " + curso.getCodigo() + " no tiene vacantes disponibles");
            }
            totalCreditos += curso.getCreditos();
        }

        if (totalCreditos > MAX_CREDITOS) {
            log.warn("RN-04 violada: total de créditos {} supera el máximo permitido para estudiante id={}",
                    totalCreditos, estudiante.getId());
            throw new ReglaNegocioException("La matrícula no puede superar " + MAX_CREDITOS + " créditos");
        }

        Matricula matricula = Matricula.builder()
                .fecha(LocalDateTime.now())
                .periodo(request.periodo())
                .estudiante(estudiante)
                .estado(EstadoMatricula.REGISTRADA)
                .totalCreditos(totalCreditos)
                .montoTotal(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .build();

        BigDecimal montoTotal = BigDecimal.ZERO;
        for (Curso curso : cursos) {
            BigDecimal costo = costoCredito.multiply(BigDecimal.valueOf(curso.getCreditos()))
                    .setScale(2, RoundingMode.HALF_UP);
            DetalleMatricula detalle = DetalleMatricula.builder()
                    .curso(curso)
                    .creditos(curso.getCreditos())
                    .costo(costo)
                    .build();
            matricula.agregarDetalle(detalle);
            montoTotal = montoTotal.add(costo);

            curso.setVacantes(curso.getVacantes() - 1);
            cursoRepository.save(curso);
        }
        matricula.setMontoTotal(montoTotal.setScale(2, RoundingMode.HALF_UP));

        matricula = matriculaRepository.save(matricula);
        log.info("Matrícula registrada id={} estudianteId={} periodo={} montoTotal={}",
                matricula.getId(), estudiante.getId(), matricula.getPeriodo(), matricula.getMontoTotal());
        return toResponse(matricula);
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaResponseDTO obtenerPorId(Long id) {
        return toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponseDTO> listar() {
        return matriculaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AnulacionResponseDTO anular(Long id) {
        Matricula matricula = buscarOFallar(id);
        if (matricula.getEstado() == EstadoMatricula.ANULADA) {
            log.warn("No se puede anular: matrícula id={} ya está anulada", id);
            throw new ReglaNegocioException("La matrícula ya está anulada");
        }

        matricula.setEstado(EstadoMatricula.ANULADA);

        List<CursoVacanteDTO> vacantes = matricula.getDetalles().stream().map(detalle -> {
            Curso curso = detalle.getCurso();
            curso.setVacantes(curso.getVacantes() + 1);
            cursoRepository.save(curso);
            return new CursoVacanteDTO(curso.getId(), curso.getCodigo(), curso.getVacantes());
        }).toList();

        matriculaRepository.save(matricula);
        log.info("Matrícula anulada id={}", id);
        return new AnulacionResponseDTO(matricula.getId(), matricula.getEstado(), vacantes);
    }

    private Matricula buscarOFallar(Long id) {
        return matriculaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Matrícula no encontrada con id " + id));
    }

    private MatriculaResponseDTO toResponse(Matricula matricula) {
        Estudiante estudiante = matricula.getEstudiante();
        List<DetalleMatriculaResponseDTO> detalles = matricula.getDetalles().stream()
                .map(d -> new DetalleMatriculaResponseDTO(
                        d.getId(),
                        d.getCurso().getId(),
                        d.getCurso().getCodigo(),
                        d.getCurso().getNombre(),
                        d.getCreditos(),
                        d.getCosto()
                )).toList();
        return new MatriculaResponseDTO(
                matricula.getId(),
                matricula.getFecha(),
                matricula.getPeriodo(),
                estudiante.getId(),
                estudiante.getCodigo(),
                estudiante.getNombres() + " " + estudiante.getApellidos(),
                matricula.getEstado(),
                matricula.getTotalCreditos(),
                matricula.getMontoTotal(),
                detalles
        );
    }
}
