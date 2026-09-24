package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.dto.CursoRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.CursoResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.entity.Curso;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.exception.SolicitudInvalidaException;
import pe.edu.upeu.MatriculaBackend.repository.CarreraRepository;
import pe.edu.upeu.MatriculaBackend.repository.CursoRepository;
import pe.edu.upeu.MatriculaBackend.repository.DetalleMatriculaRepository;
import pe.edu.upeu.MatriculaBackend.service.service.CursoService;
import pe.edu.upeu.MatriculaBackend.specification.CursoSpecifications;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class CursoServiceImpl implements CursoService {

    private static final Set<String> CAMPOS_ORDEN_VALIDOS = Set.of("nombre", "creditos", "vacantes");

    private final CursoRepository cursoRepository;
    private final CarreraRepository carreraRepository;
    private final DetalleMatriculaRepository detalleMatriculaRepository;

    @Override
    @Transactional
    public CursoResponseDTO crear(CursoRequestDTO request) {
        Carrera carrera = buscarCarreraOFallar(request.carreraId());
        validarCodigoUnico(request.codigo(), null);
        Curso curso = Curso.builder()
                .codigo(request.codigo())
                .nombre(request.nombre())
                .creditos(request.creditos())
                .ciclo(request.ciclo())
                .vacantes(request.vacantes())
                .estado(request.estado())
                .carrera(carrera)
                .build();
        curso = cursoRepository.save(curso);
        log.info("Curso creado id={} codigo={}", curso.getId(), curso.getCodigo());
        return toResponse(curso);
    }

    @Override
    @Transactional(readOnly = true)
    public CursoResponseDTO obtenerPorId(Long id) {
        return toResponse(buscarOFallar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> listar() {
        return cursoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CursoResponseDTO actualizar(Long id, CursoRequestDTO request) {
        Curso curso = buscarOFallar(id);
        Carrera carrera = buscarCarreraOFallar(request.carreraId());
        validarCodigoUnico(request.codigo(), id);
        curso.setCodigo(request.codigo());
        curso.setNombre(request.nombre());
        curso.setCreditos(request.creditos());
        curso.setCiclo(request.ciclo());
        curso.setVacantes(request.vacantes());
        curso.setEstado(request.estado());
        curso.setCarrera(carrera);
        curso = cursoRepository.save(curso);
        log.info("Curso actualizado id={}", curso.getId());
        return toResponse(curso);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Curso curso = buscarOFallar(id);
        if (detalleMatriculaRepository.existsByCursoId(id)) {
            log.warn("No se puede eliminar el curso id={} porque tiene matrículas asociadas", id);
            throw new ReglaNegocioException("No se puede eliminar el curso porque tiene matrículas asociadas");
        }
        cursoRepository.delete(curso);
        log.info("Curso eliminado id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> listarPorCarrera(Long carreraId) {
        buscarCarreraOFallar(carreraId);
        return cursoRepository.findByCarreraId(carreraId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoResponseDTO> buscar(String nombre, Long carreraId, Integer ciclo, Boolean conVacantes,
                                          String ordenarPor, String direccion) {
        String campo = (ordenarPor == null || ordenarPor.isBlank()) ? "nombre" : ordenarPor.trim().toLowerCase();
        if (!CAMPOS_ORDEN_VALIDOS.contains(campo)) {
            throw new SolicitudInvalidaException("El campo de orden debe ser uno de: " + CAMPOS_ORDEN_VALIDOS);
        }
        Sort.Direction dir = Sort.Direction.ASC;
        if (direccion != null && !direccion.isBlank()) {
            if (!"ASC".equalsIgnoreCase(direccion) && !"DESC".equalsIgnoreCase(direccion)) {
                throw new SolicitudInvalidaException("La dirección debe ser ASC o DESC");
            }
            dir = Sort.Direction.fromString(direccion);
        }

        Specification<Curso> spec = (root, query, cb) -> cb.conjunction();
        if (nombre != null && !nombre.isBlank()) {
            spec = spec.and(CursoSpecifications.nombreContiene(nombre));
        }
        if (carreraId != null) {
            spec = spec.and(CursoSpecifications.deCarrera(carreraId));
        }
        if (ciclo != null) {
            spec = spec.and(CursoSpecifications.deCiclo(ciclo));
        }
        if (conVacantes != null) {
            spec = spec.and(CursoSpecifications.conVacantes(conVacantes));
        }

        return cursoRepository.findAll(spec, Sort.by(dir, campo)).stream().map(this::toResponse).toList();
    }

    private void validarCodigoUnico(String codigo, Long idActual) {
        cursoRepository.buscarPorCodigo(codigo).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un curso con ese código");
            }
        });
    }

    private Curso buscarOFallar(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Curso no encontrado con id " + id));
    }

    private Carrera buscarCarreraOFallar(Long carreraId) {
        return carreraRepository.findById(carreraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada con id " + carreraId));
    }

    private CursoResponseDTO toResponse(Curso curso) {
        return new CursoResponseDTO(
                curso.getId(),
                curso.getCodigo(),
                curso.getNombre(),
                curso.getCreditos(),
                curso.getCiclo(),
                curso.getVacantes(),
                curso.getEstado(),
                curso.getCarrera().getId(),
                curso.getCarrera().getNombre(),
                curso.getFechaCreacion(),
                curso.getFechaModificacion()
        );
    }
}
