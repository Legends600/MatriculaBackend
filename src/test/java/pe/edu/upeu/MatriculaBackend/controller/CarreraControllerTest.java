package pe.edu.upeu.MatriculaBackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.edu.upeu.MatriculaBackend.dto.CarreraRequestDTO;
import pe.edu.upeu.MatriculaBackend.dto.CarreraResponseDTO;
import pe.edu.upeu.MatriculaBackend.service.service.CarreraService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarreraController.class)
class CarreraControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CarreraService carreraService;

    @Test
    void listar_debeRetornar200ConListaDeCarreras() throws Exception {
        CarreraResponseDTO carrera = new CarreraResponseDTO(1L, "Ingeniería de Sistemas", "desc", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(carreraService.listar()).thenReturn(List.of(carrera));

        mockMvc.perform(get("/api/v1/carreras"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ingeniería de Sistemas"));
    }

    @Test
    void crear_debeRetornar400_cuandoNombreEsInvalido() throws Exception {
        CarreraRequestDTO invalida = new CarreraRequestDTO("AB", "desc", true);

        mockMvc.perform(post("/api/v1/carreras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalida)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.nombre").exists());
    }

    @Test
    void crear_debeRetornar201_cuandoDatosSonValidos() throws Exception {
        CarreraRequestDTO request = new CarreraRequestDTO("Ingeniería de Sistemas", "desc", true);
        CarreraResponseDTO response = new CarreraResponseDTO(1L, "Ingeniería de Sistemas", "desc", true,
                LocalDateTime.now(), LocalDateTime.now());
        when(carreraService.crear(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/carreras")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }
}
