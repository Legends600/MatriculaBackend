package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.dto.EstudianteRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.EstudianteResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.entity.Estudiante;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.repository.CarreraRepository;
import pe.edu.upeu.MatriculaBackend.repository.EstudianteRepository;
import pe.edu.upeu.MatriculaBackend.service.service.EstudianteService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EstudianteServiceImpl implements EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final CarreraRepository carreraRepository;

    @Override
    @Transactional
    public EstudianteResponseDTO registrar(EstudianteRequestDTO request) {
        Carrera carrera = buscarCarreraOFallar(request.carreraId());
        validarCodigoUnico(request.codigo(), null);
        validarDniUnico(request.dni(), null);
        Estudiante estudiante = Estudiante.builder()
                .codigo(request.codigo())
                .dni(request.dni())
                .nombres(request.nombres())
                .apellidos(request.apellidos())
                .email(request.email())
                .estado(request.estado())
                .carrera(carrera)
                .build();
        estudiante = estudianteRepository.save(estudiante);
        log.info("Estudiante registrado id={} codigo={}", estudiante.getId(), estudiante.getCodigo());
        return toResponse(estudiante);
    }

    @Override
    public EstudianteResponseDTO obtenerPorId(Long id) {
        return toResponse(buscarOFallar(id));
    }

    @Override
    public List<EstudianteResponseDTO> listar() {
        return estudianteRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public EstudianteResponseDTO actualizar(Long id, EstudianteRequestDTO request) {
        Estudiante estudiante = buscarOFallar(id);
        Carrera carrera = buscarCarreraOFallar(request.carreraId());
        validarCodigoUnico(request.codigo(), id);
        validarDniUnico(request.dni(), id);
        estudiante.setCodigo(request.codigo());
        estudiante.setDni(request.dni());
        estudiante.setNombres(request.nombres());
        estudiante.setApellidos(request.apellidos());
        estudiante.setEmail(request.email());
        estudiante.setEstado(request.estado());
        estudiante.setCarrera(carrera);
        estudiante = estudianteRepository.save(estudiante);
        log.info("Estudiante actualizado id={}", estudiante.getId());
        return toResponse(estudiante);
    }

    private void validarCodigoUnico(String codigo, Long idActual) {
        estudianteRepository.findByCodigo(codigo).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un estudiante con ese código");
            }
        });
    }

    private void validarDniUnico(String dni, Long idActual) {
        estudianteRepository.findByDni(dni).ifPresent(existente -> {
            if (!existente.getId().equals(idActual)) {
                throw new ReglaNegocioException("Ya existe un estudiante con ese DNI");
            }
        });
    }

    private Estudiante buscarOFallar(Long id) {
        return estudianteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Estudiante no encontrado con id " + id));
    }

    private Carrera buscarCarreraOFallar(Long carreraId) {
        return carreraRepository.findById(carreraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrera no encontrada con id " + carreraId));
    }

    private EstudianteResponseDTO toResponse(Estudiante estudiante) {
        return new EstudianteResponseDTO(
                estudiante.getId(),
                estudiante.getCodigo(),
                estudiante.getDni(),
                estudiante.getNombres(),
                estudiante.getApellidos(),
                estudiante.getEmail(),
                estudiante.getEstado(),
                estudiante.getCarrera().getId(),
                estudiante.getCarrera().getNombre(),
                estudiante.getFechaCreacion(),
                estudiante.getFechaModificacion()
        );
    }
}
