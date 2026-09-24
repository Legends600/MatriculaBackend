package pe.edu.upeu.MatriculaBackend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.dto.CarreraRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.CarreraResponseDTO;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.exception.ReglaNegocioException;
import pe.edu.upeu.MatriculaBackend.exception.RecursoNoEncontradoException;
import pe.edu.upeu.MatriculaBackend.repository.CarreraRepository;
import pe.edu.upeu.MatriculaBackend.service.service.CarreraService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CarreraServiceImpl implements CarreraService {

    private final CarreraRepository carreraRepository;

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
        carreraRepository.delete(carrera);
        log.info("Carrera eliminada id={}", id);
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
}
