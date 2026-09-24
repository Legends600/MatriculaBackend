package pe.edu.upeu.MatriculaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.MatriculaBackend.dto.MatriculadosPorCursoDTO;
import pe.edu.upeu.MatriculaBackend.entity.DetalleMatricula;

import java.util.List;

public interface DetalleMatriculaRepository extends JpaRepository<DetalleMatricula, Long> {

    boolean existsByCursoId(Long cursoId);

    @Query("""
            SELECT new pe.edu.upeu.MatriculaBackend.dto.MatriculadosPorCursoDTO(
                c.codigo, c.nombre, COUNT(d.id), COALESCE(SUM(d.costo), 0.0))
            FROM DetalleMatricula d
            JOIN d.curso c
            JOIN d.matricula m
            WHERE m.estado = pe.edu.upeu.MatriculaBackend.enums.EstadoMatricula.REGISTRADA
              AND (:periodo IS NULL OR m.periodo = :periodo)
              AND (:carreraId IS NULL OR c.carrera.id = :carreraId)
            GROUP BY c.codigo, c.nombre
            ORDER BY c.codigo
            """)
    List<MatriculadosPorCursoDTO> matriculadosPorCurso(@Param("periodo") String periodo,
                                                         @Param("carreraId") Long carreraId);
}
