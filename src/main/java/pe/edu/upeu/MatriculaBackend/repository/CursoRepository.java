package pe.edu.upeu.MatriculaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.MatriculaBackend.entity.Curso;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long>, JpaSpecificationExecutor<Curso> {

    List<Curso> findByCarreraId(Long carreraId);

    boolean existsByCarreraId(Long carreraId);

    @Query("SELECT c FROM Curso c WHERE UPPER(c.codigo) = UPPER(:codigo)")
    Optional<Curso> buscarPorCodigo(@Param("codigo") String codigo);
}
