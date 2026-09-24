package pe.edu.upeu.MatriculaBackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.MatriculaBackend.entity.Carrera;
import pe.edu.upeu.MatriculaBackend.entity.Curso;
import pe.edu.upeu.MatriculaBackend.entity.Estudiante;
import pe.edu.upeu.MatriculaBackend.repository.CarreraRepository;
import pe.edu.upeu.MatriculaBackend.repository.CursoRepository;
import pe.edu.upeu.MatriculaBackend.repository.EstudianteRepository;

/**
 * Datos semilla mínimos exigidos por el enunciado (sección 3.3), activo solo en dev.
 * Los cursos con vacantes de Ingeniería de Sistemas suman 21 créditos (> 20) para poder probar RN-04.
 */
@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CarreraRepository carreraRepository;
    private final CursoRepository cursoRepository;
    private final EstudianteRepository estudianteRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (carreraRepository.count() > 0) {
            log.info("Datos semilla ya existen, se omite la carga inicial");
            return;
        }

        Carrera sistemas = carreraRepository.save(Carrera.builder()
                .nombre("Ingeniería de Sistemas")
                .descripcion("Formación en desarrollo de software y sistemas de información")
                .estado(true)
                .build());

        Carrera civil = carreraRepository.save(Carrera.builder()
                .nombre("Ingeniería Civil")
                .descripcion("Formación en diseño y construcción de infraestructura civil")
                .estado(true)
                .build());

        Carrera arquitectura = carreraRepository.save(Carrera.builder()
                .nombre("Arquitectura")
                .descripcion("Formación en diseño arquitectónico y urbanismo")
                .estado(true)
                .build());

        cursoRepository.save(curso("IS101", "Programación I", 6, 1, 30, true, sistemas));
        cursoRepository.save(curso("IS102", "Estructura de Datos", 5, 2, 25, true, sistemas));
        cursoRepository.save(curso("IS103", "Base de Datos", 5, 3, 20, true, sistemas));
        cursoRepository.save(curso("IS104", "Ingeniería de Software", 5, 5, 15, true, sistemas));
        cursoRepository.save(curso("IS105", "Redes y Comunicaciones", 3, 4, 0, true, sistemas));

        cursoRepository.save(curso("CI101", "Mecánica de Suelos", 4, 3, 20, true, civil));
        cursoRepository.save(curso("CI102", "Resistencia de Materiales", 4, 2, 0, true, civil));
        cursoRepository.save(curso("CI103", "Topografía", 3, 1, 18, true, civil));
        cursoRepository.save(curso("CI104", "Estructuras Metálicas", 4, 6, 12, false, civil));

        cursoRepository.save(curso("AR101", "Historia de la Arquitectura", 3, 1, 25, true, arquitectura));
        cursoRepository.save(curso("AR102", "Diseño Arquitectónico I", 5, 2, 20, true, arquitectura));
        cursoRepository.save(curso("AR103", "Urbanismo", 4, 4, 15, true, arquitectura));

        estudianteRepository.save(estudiante("202100001", "70000001", "Juan", "Pérez López",
                "juan.perez@upeu.edu.pe", true, sistemas));
        estudianteRepository.save(estudiante("202100002", "70000002", "María", "Gómez Rivas",
                "maria.gomez@upeu.edu.pe", true, sistemas));
        estudianteRepository.save(estudiante("202100003", "70000003", "Carlos", "Ramírez Soto",
                "carlos.ramirez@upeu.edu.pe", false, sistemas));
        estudianteRepository.save(estudiante("202100004", "70000004", "Ana", "Torres Vega",
                "ana.torres@upeu.edu.pe", true, civil));
        estudianteRepository.save(estudiante("202100005", "70000005", "Luis", "Fernández Cruz",
                "luis.fernandez@upeu.edu.pe", true, civil));
        estudianteRepository.save(estudiante("202100006", "70000006", "Rosa", "Mendoza Díaz",
                "rosa.mendoza@upeu.edu.pe", true, arquitectura));

        log.info("Datos semilla cargados: 3 carreras, 12 cursos, 6 estudiantes");
    }

    private Curso curso(String codigo, String nombre, int creditos, int ciclo, int vacantes, boolean estado,
                         Carrera carrera) {
        return Curso.builder()
                .codigo(codigo)
                .nombre(nombre)
                .creditos(creditos)
                .ciclo(ciclo)
                .vacantes(vacantes)
                .estado(estado)
                .carrera(carrera)
                .build();
    }

    private Estudiante estudiante(String codigo, String dni, String nombres, String apellidos, String email,
                                   boolean estado, Carrera carrera) {
        return Estudiante.builder()
                .codigo(codigo)
                .dni(dni)
                .nombres(nombres)
                .apellidos(apellidos)
                .email(email)
                .estado(estado)
                .carrera(carrera)
                .build();
    }
}
