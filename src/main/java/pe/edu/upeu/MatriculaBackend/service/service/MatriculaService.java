package pe.edu.upeu.MatriculaBackend.service.service;

import pe.edu.upeu.MatriculaBackend.dto.AnulacionResponseDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.MatriculaResponseDTO;

import java.util.List;

public interface MatriculaService {

    MatriculaResponseDTO registrar(MatriculaRequestDTO request);

    MatriculaResponseDTO obtenerPorId(Long id);

    List<MatriculaResponseDTO> listar();

    AnulacionResponseDTO anular(Long id);
}
