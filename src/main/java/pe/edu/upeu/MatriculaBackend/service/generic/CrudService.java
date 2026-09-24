package pe.edu.upeu.MatriculaBackend.service.generic;

import java.util.List;

public interface CrudService<REQ, RES, ID> {

    RES crear(REQ request);

    RES obtenerPorId(ID id);

    List<RES> listar();

    RES actualizar(ID id, REQ request);

    void eliminar(ID id);
}
