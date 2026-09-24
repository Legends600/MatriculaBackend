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
 * Datos semilla del Anexo A (datos_semilla.sql), completados hasta los doce
 * cursos y seis estudiantes exigidos en la sección 3.3. Activo solo en dev.
 * Los cursos con vacantes de Ingeniería de Sistemas (IS401 a IS503, sin
 * contar IS404 que tiene 0) suman 22 créditos (> 20) para poder probar RN-04
 * con el caso de prueba CP-11 del Anexo B.
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
                .descripcion("EP Ingeniería de Sistemas")
                .estado(true)
                .build());

        Carrera civil = carreraRepository.save(Carrera.builder()
                .nombre("Ingeniería Civil")
                .descripcion("EP Ingeniería Civil")
                .estado(true)
                .build());

        Carrera arquitectura = carreraRepository.save(Carrera.builder()
                .nombre("Arquitectura")
                .descripcion("EP Arquitectura y Urbanismo")
                .estado(true)
                .build());

        // Cursos del Anexo A (IS401 a AR401)
        cursoRepository.save(curso("IS401", "Lenguaje de Programación II", 3, 4, 30, true, sistemas));
        cursoRepository.save(curso("IS402", "Base de Datos II", 4, 4, 25, true, sistemas));
        cursoRepository.save(curso("IS403", "Ingeniería de Requisitos", 3, 4, 2, true, sistemas));
        cursoRepository.save(curso("IS404", "Estadística Aplicada", 3, 4, 0, true, sistemas));
        cursoRepository.save(curso("IS501", "Arquitectura de Software", 4, 5, 20, true, sistemas));
        cursoRepository.save(curso("IS502", "Sistemas Operativos", 4, 5, 15, true, sistemas));
        cursoRepository.save(curso("IS503", "Redes de Computadoras", 4, 5, 20, true, sistemas));
        cursoRepository.save(curso("IC401", "Mecánica de Suelos", 4, 4, 30, true, civil));
        cursoRepository.save(curso("AR401", "Taller de Diseño IV", 6, 4, 15, true, arquitectura));

        // Completa hasta los doce cursos: otro con 0 vacantes y uno inactivo
        cursoRepository.save(curso("IC402", "Resistencia de Materiales", 4, 5, 0, true, civil));
        cursoRepository.save(curso("IC403", "Topografía", 3, 4, 20, false, civil));
        cursoRepository.save(curso("AR402", "Historia de la Arquitectura", 3, 4, 25, true, arquitectura));

        // Estudiantes del Anexo A (Ana, Jorge, María, Carlos)
        Estudiante ana = estudianteRepository.save(estudiante("202410001", "71234567", "Ana Lucía",
                "Quispe Mamani", "ana.quispe@upeu.edu.pe", true, sistemas));
        Estudiante jorge = estudianteRepository.save(estudiante("202410002", "72345678", "Jorge Luis",
                "Condori Apaza", "jorge.condori@upeu.edu.pe", true, sistemas));
        Estudiante maria = estudianteRepository.save(estudiante("202410003", "73456789", "María Elena",
                "Huamán Torres", "maria.huaman@upeu.edu.pe", true, civil));
        Estudiante carlos = estudianteRepository.save(estudiante("202410004", "74567890", "Carlos Alberto",
                "Mamani Flores", "carlos.mamani@upeu.edu.pe", false, sistemas));

        // Completa hasta los seis estudiantes
        estudianteRepository.save(estudiante("202410005", "75678901", "Rosa Elvira",
                "Ttito Choque", "rosa.ttito@upeu.edu.pe", true, arquitectura));
        estudianteRepository.save(estudiante("202410006", "76789012", "Luis Fernando",
                "Apaza Vilca", "luis.apaza@upeu.edu.pe", true, civil));

        log.info("Datos semilla cargados: 3 carreras, 12 cursos, 6 estudiantes (Ana id={}, Jorge id={}, María id={}, Carlos id={})",
                ana.getId(), jorge.getId(), maria.getId(), carlos.getId());
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
