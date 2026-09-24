package pe.edu.upeu.MatriculaBackend.service.service;

import pe.edu.upeu.MatriculaBackend.dto.CarreraRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.CarreraResponseDTO;
import pe.edu.upeu.MatriculaBackend.dto.CursoResponseDTO;
import pe.edu.upeu.MatriculaBackend.service.generic.CrudService;

import java.util.List;

public interface CarreraService extends CrudService<CarreraRequestDTO, CarreraResponseDTO, Long> {

    List<CursoResponseDTO> listarCursosDeCarrera(Long carreraId);
}
