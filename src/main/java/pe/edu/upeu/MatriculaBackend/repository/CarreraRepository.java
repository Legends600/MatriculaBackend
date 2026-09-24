package pe.edu.upeu.MatriculaBackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;

import java.util.Optional;

public interface CarreraRepository extends JpaRepository<Carrera, Long> {

    @Query("SELECT c FROM Carrera c WHERE UPPER(REPLACE(c.nombre, ' ', '')) = UPPER(REPLACE(:nombre, ' ', ''))")
    Optional<Carrera> buscarPorNombreNormalizado(@Param("nombre") String nombre);
}
