package pe.edu.upeu.MatriculaBackend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.MatriculaBackend.dto.HealthResponseDTO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

@Slf4j
@Tag(name = "Salud")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/health")
    public ResponseEntity<HealthResponseDTO> health() {
        boolean activa;
        try (Connection connection = dataSource.getConnection()) {
            activa = connection.isValid(2);
        } catch (SQLException e) {
            log.error("Error al comprobar la conexión a la base de datos", e);
            activa = false;
        }

        String estadoDatabase = activa ? "UP" : "DOWN";
        HealthResponseDTO body = new HealthResponseDTO(activa ? "UP" : "DOWN", estadoDatabase, LocalDateTime.now());
        return activa
                ? ResponseEntity.ok(body)
                : ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }
}
