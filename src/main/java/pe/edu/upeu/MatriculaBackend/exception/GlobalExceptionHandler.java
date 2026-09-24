package pe.edu.upeu.MatriculaBackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.upeu.MatriculaBackend.dto.ErrorResponseDTO;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> manejarValidacion(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));
        log.warn("Validación fallida en {}: {}", request.getRequestURI(), errores);
        return construir(HttpStatus.BAD_REQUEST, "Error de validación", request, errores);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarConstraint(ConstraintViolationException ex,
                                                                HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getConstraintViolations()
                .forEach(v -> errores.put(v.getPropertyPath().toString(), v.getMessage()));
        log.warn("Restricción violada en {}: {}", request.getRequestURI(), errores);
        return construir(HttpStatus.BAD_REQUEST, "Error de validación", request, errores);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                                    HttpServletRequest request) {
        log.warn("Cuerpo de la solicitud ilegible en {}", request.getRequestURI());
        return construir(HttpStatus.BAD_REQUEST, "El cuerpo de la solicitud no es un JSON válido", request, null);
    }

    @ExceptionHandler(SolicitudInvalidaException.class)
    public ResponseEntity<ErrorResponseDTO> manejarSolicitudInvalida(SolicitudInvalidaException ex,
                                                                      HttpServletRequest request) {
        log.warn("Solicitud inválida en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponseDTO> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                                  HttpServletRequest request) {
        log.warn("Recurso no encontrado en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponseDTO> manejarReglaNegocio(ReglaNegocioException ex,
                                                                  HttpServletRequest request) {
        log.warn("Regla de negocio violada en {}: {}", request.getRequestURI(), ex.getMessage());
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> manejarIntegridad(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Violación de integridad de datos en {}", request.getRequestURI());
        return construir(HttpStatus.CONFLICT, "El recurso no se puede modificar por restricciones de integridad", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error interno en {}", request.getRequestURI(), ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request, null);
    }

    private ResponseEntity<ErrorResponseDTO> construir(HttpStatus status, String message, HttpServletRequest request,
                                                         Map<String, String> validationErrors) {
        ErrorResponseDTO body = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                validationErrors
        );
        return ResponseEntity.status(status).body(body);
    }
}
