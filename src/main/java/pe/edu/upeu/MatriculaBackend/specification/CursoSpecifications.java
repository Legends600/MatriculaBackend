package pe.edu.upeu.MatriculaBackend.specification;

import org.springframework.data.jpa.domain.Specification;
import pe.edu.upeu.MatriculaBackend.entity.Curso;

public final class CursoSpecifications {

    private CursoSpecifications() {
    }

    public static Specification<Curso> nombreContiene(String nombre) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%");
    }

    public static Specification<Curso> deCarrera(Long carreraId) {
        return (root, query, cb) -> cb.equal(root.get("carrera").get("id"), carreraId);
    }

    public static Specification<Curso> deCiclo(Integer ciclo) {
        return (root, query, cb) -> cb.equal(root.get("ciclo"), ciclo);
    }

    public static Specification<Curso> conVacantes(boolean conVacantes) {
        return conVacantes
                ? (root, query, cb) -> cb.greaterThan(root.get("vacantes"), 0)
                : (root, query, cb) -> cb.equal(root.get("vacantes"), 0);
    }
}
