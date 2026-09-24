package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.dto.CarreraRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.CarreraResponseDTO;
import pe.edu.upeu.MatriculaBackend.dto.CursoResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.entity.Curso;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.repository.CarreraRepository;
import pe.edu.upeu.MatriculaBackend.repository.CursoRepository;
import pe.edu.upeu.MatriculaBackend.service.service.CarreraService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarreraServiceImpl implements CarreraService {

    private final CarreraRepository carreraRepository;
    private final CursoRepository cursoRepository;

    @Override
    @Transactional
    public CarreraResponseDTO crear(CarreraRequestDTO request) {
        validarNombreUnico(request.nombre(), null);
        Carrera carrera = Carrera.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .estado(request.estado())
                .build();
        carrera = carreraRepository.save(carrera);
        log.info("Carrera creada id={} nombre={}", carrera.getId(), carrera.getNombre());
        return toResponse(carrera);
    }

    @Override
    public CarreraResponseDTO obtenerPorId(Long id) {
        return toResponse(buscarOFallar(id));
    }

    @Override
    public List<CarreraResponseDTO> listar() {
        return carreraRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CarreraResponseDTO actualizar(Long id, CarreraRequestDTO request) {
        Carrera carrera = buscarOFallar(id);
        validarNombreUnico(request.nombre(), id);
        carrera.setNombre(request.nombre());
        carrera.setDescripcion(request.descripcion());
        carrera.setEstado(request.estado());
        carrera = carreraRepository.save(carrera);
        log.info("Carrera actualizada id={}", carrera.getId());
        return toResponse(carrera);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Carrera carrera = buscarOFallar(id);
        if (cursoRepository.existsByCarreraId(id)) {
            log.warn("No se puede eliminar la carrera id={} porque tiene cursos asociados", id);
            throw new ReglaNegocioException("No se puede eliminar la carrera porque tiene cursos asociados");
        }
        carreraRepository.delete(carrera);
        log.info("Carrera eliminada id={}", id);
    }

    @Override
    public List<CursoResponseDTO> listarCursosDeCarrera(Long carreraId) {
        buscarOFallar(carreraId);
        return cursoRepository.findByCarreraId(carreraId).stream().map(this::toCursoResponse).toList();
    }

    private void validarNombreUnico(String nombre, Long idActual) {
        carreraRepository.buscarPorNombreNormalizado(nombre).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe una carrera con ese nombre");
            }
        });
    }

    private Carrera buscarOFallar(Long id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada con id " + id));
    }

    private CarreraResponseDTO toResponse(Carrera carrera) {
        return new CarreraResponseDTO(
                carrera.getId(),
                carrera.getNombre(),
                carrera.getDescripcion(),
                carrera.getEstado(),
                carrera.getFechaCreacion(),
                carrera.getFechaModificacion()
        );
    }

    private CursoResponseDTO toCursoResponse(Curso curso) {
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
