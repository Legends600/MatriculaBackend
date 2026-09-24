package pe.edu.upeu.MatriculaBackend.service.service;

import pe.edu.upeu.MatriculaBackend.dto.EstudianteRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.EstudianteResponseDTO;

import java.util.List;

public interface EstudianteService {

    EstudianteResponseDTO registrar(EstudianteRequestDTO request);

    EstudianteResponseDTO obtenerPorId(Long id);

    List<EstudianteResponseDTO> listar();

    EstudianteResponseDTO actualizar(Long id, EstudianteRequestDTO request);
}
