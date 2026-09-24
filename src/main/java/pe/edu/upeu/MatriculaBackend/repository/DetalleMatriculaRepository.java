package pe.edu.upeu.MatriculaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.MatriculaBackend.entity.DetalleMatricula;

public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, Long> {

    boolean existsByCursoId(Long cursoId);
}
