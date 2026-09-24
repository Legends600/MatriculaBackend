package pe.edu.upeu.MatriculaBackend.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pe.edu.upeu.MatriculaBackend.dto.DetalleMatriculaRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.entity.Curso;
import pe.edu.upeu.MatriculaBackend.entity.Estudiante;
import pe.edu.upeu.MatriculaBackend.entity.Matricula;
import pe.edu.upeu.MatriculaBackend.enums.EstadoMatricula;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.repository.CursoRepository;
import pe.edu.upeu.MatriculaBackend.repository.EstudianteRepository;
import pe.edu.upeu.MatriculaBackend.repository.MatriculaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Pruebas de las reglas de negocio RN-01 a RN-04, que viven en MatriculaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class MatriculaServiceImplTest {

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private EstudianteRepository estudianteRepository;
    @Mock
    private CursoRepository cursoRepository;

    @InjectMocks
    private MatriculaServiceImpl matriculaService;

    private Carrera carreraSistemas;
    private Estudiante estudianteActivo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(matriculaService, "costoCredito", new BigDecimal("120.00"));
        carreraSistemas = Carrera.builder().id(1L).nombre("Ingeniería de Sistemas").estado(true).build();
        estudianteActivo = Estudiante.builder().id(1L).codigo("202100001").dni("70000001")
                .nombres("Juan").apellidos("Pérez").email("juan@upeu.edu.pe").estado(true)
                .carrera(carreraSistemas).build();
    }

    @Test
    void registrar_debeLanzarConflicto_cuandoEstudianteEstaInactivo() {
        Estudiante inactivo = Estudiante.builder().id(2L).estado(false).carrera(carreraSistemas).build();
        when(estudianteRepository.findById(2L)).thenReturn(Optional.of(inactivo));

        MatriculaRequestDTO request = new MatriculaRequestDTO(2L, "2026-2",
                List.of(new DetalleMatriculaRequestDTO(1L)));

        assertThatThrownBy(() -> matriculaService.registrar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("activo");
    }

    @Test
    void registrar_debeLanzarConflicto_cuandoCursoNoTieneVacantes() {
        Curso cursoSinVacantes = Curso.builder().id(10L).codigo("IS105").creditos(3).vacantes(0)
                .estado(true).carrera(carreraSistemas).build();

        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo));
        when(matriculaRepository.existsByEstudianteIdAndPeriodoAndEstado(1L, "2026-2", EstadoMatricula.REGISTRADA))
                .thenReturn(false);
        when(cursoRepository.findById(10L)).thenReturn(Optional.of(cursoSinVacantes));

        MatriculaRequestDTO request = new MatriculaRequestDTO(1L, "2026-2",
                List.of(new DetalleMatriculaRequestDTO(10L)));

        assertThatThrownBy(() -> matriculaService.registrar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("vacantes");
    }

    @Test
    void registrar_debeLanzarConflicto_cuandoYaExisteMatriculaRegistradaEnElPeriodo() {
        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo));
        when(matriculaRepository.existsByEstudianteIdAndPeriodoAndEstado(1L, "2026-2", EstadoMatricula.REGISTRADA))
                .thenReturn(true);

        MatriculaRequestDTO request = new MatriculaRequestDTO(1L, "2026-2",
                List.of(new DetalleMatriculaRequestDTO(10L)));

        assertThatThrownBy(() -> matriculaService.registrar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("periodo");
    }

    @Test
    void registrar_debeLanzarConflicto_cuandoSuperaElMaximoDeCreditos() {
        Curso curso1 = Curso.builder().id(1L).codigo("IS101").creditos(6).vacantes(10).estado(true).carrera(carreraSistemas).build();
        Curso curso2 = Curso.builder().id(2L).codigo("IS102").creditos(6).vacantes(10).estado(true).carrera(carreraSistemas).build();
        Curso curso3 = Curso.builder().id(3L).codigo("IS103").creditos(6).vacantes(10).estado(true).carrera(carreraSistemas).build();
        Curso curso4 = Curso.builder().id(4L).codigo("IS104").creditos(6).vacantes(10).estado(true).carrera(carreraSistemas).build();

        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo));
        when(matriculaRepository.existsByEstudianteIdAndPeriodoAndEstado(1L, "2026-2", EstadoMatricula.REGISTRADA))
                .thenReturn(false);
        when(cursoRepository.findById(1L)).thenReturn(Optional.of(curso1));
        when(cursoRepository.findById(2L)).thenReturn(Optional.of(curso2));
        when(cursoRepository.findById(3L)).thenReturn(Optional.of(curso3));
        when(cursoRepository.findById(4L)).thenReturn(Optional.of(curso4));

        MatriculaRequestDTO request = new MatriculaRequestDTO(1L, "2026-2",
                List.of(new DetalleMatriculaRequestDTO(1L), new DetalleMatriculaRequestDTO(2L),
                        new DetalleMatriculaRequestDTO(3L), new DetalleMatriculaRequestDTO(4L)));

        assertThatThrownBy(() -> matriculaService.registrar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("20 créditos");
    }

    @Test
    void registrar_debeLanzarConflicto_cuandoCursoNoPerteneceALaCarreraDelEstudiante() {
        Carrera otraCarrera = Carrera.builder().id(2L).nombre("Arquitectura").estado(true).build();
        Curso cursoDeOtraCarrera = Curso.builder().id(20L).codigo("AR101").creditos(4).vacantes(10)
                .estado(true).carrera(otraCarrera).build();

        when(estudianteRepository.findById(1L)).thenReturn(Optional.of(estudianteActivo));
        when(matriculaRepository.existsByEstudianteIdAndPeriodoAndEstado(1L, "2026-2", EstadoMatricula.REGISTRADA))
                .thenReturn(false);
        when(cursoRepository.findById(20L)).thenReturn(Optional.of(cursoDeOtraCarrera));

        MatriculaRequestDTO request = new MatriculaRequestDTO(1L, "2026-2",
                List.of(new DetalleMatriculaRequestDTO(20L)));

        assertThatThrownBy(() -> matriculaService.registrar(request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("propia carrera");
    }

    @Test
    void historialPorEstudiante_debeLanzarNoEncontrado_cuandoEstudianteNoExiste() {
        when(estudianteRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> matriculaService.historialPorEstudiante(999L, null))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("Estudiante no encontrado");
    }

    @Test
    void historialPorEstudiante_debeRetornarMatriculasOrdenadasDeLaMasRecienteALaMasAntigua() {
        Matricula reciente = Matricula.builder().id(2L).fecha(LocalDateTime.now())
                .periodo("2026-2").estudiante(estudianteActivo).estado(EstadoMatricula.REGISTRADA)
                .totalCreditos(4).montoTotal(new BigDecimal("480.00")).build();
        Matricula antigua = Matricula.builder().id(1L).fecha(LocalDateTime.now().minusMonths(6))
                .periodo("2026-1").estudiante(estudianteActivo).estado(EstadoMatricula.REGISTRADA)
                .totalCreditos(4).montoTotal(new BigDecimal("480.00")).build();

        when(estudianteRepository.existsById(1L)).thenReturn(true);
        when(matriculaRepository.buscarHistorialPorEstudiante(1L, null))
                .thenReturn(List.of(reciente, antigua));

        List<MatriculaResponseDTO> historial = matriculaService.historialPorEstudiante(1L, null);

        assertThat(historial).hasSize(2);
        assertThat(historial.get(0).id()).isEqualTo(2L);
        assertThat(historial.get(1).id()).isEqualTo(1L);
    }
}
