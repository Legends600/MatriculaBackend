# Matrícula API

API REST para la gestión de carreras, cursos, estudiantes y matrículas de EduAndes, construida con Spring Boot 4, Java 21 y Oracle.

## Estructura de paquetes

```
src/main/java/pe/edu/upeu/MatriculaBackend/
├── MatriculaBackendApplication.java
├── config/                  OpenApiConfig, WebConfig (CORS), DataSeeder
├── controller/               CarreraController, CursoController, EstudianteController,
│                              MatriculaController, ReporteController, HealthController
├── dto/                      *RequestDTO / *ResponseDTO de cada recurso, ErrorResponseDTO,
│                              MatriculadosPorCursoDTO (proyección del reporte)
├── entity/                   Carrera, Curso, Estudiante, Matricula, DetalleMatricula,
│                              Auditable (fecha_creacion / fecha_modificacion)
├── enums/                     EstadoMatricula
├── exception/                 RecursoNoEncontradoException, ReglaNegocioException,
│                              SolicitudInvalidaException, GlobalExceptionHandler
├── repository/                CarreraRepository, CursoRepository, EstudianteRepository,
│                              MatriculaRepository, DetalleMatriculaRepository
├── service/
│   ├── generic/                CrudService<REQ, RES, ID>
│   ├── service/                CarreraService, CursoService, EstudianteService,
│   │                            MatriculaService, ReporteService
│   └── impl/                   *ServiceImpl (una por cada interfaz de service/service)
└── specification/              CursoSpecifications (filtros combinables de /cursos/buscar)

src/main/resources/
├── application.yaml           Configuración común (perfil activo, Jackson, matricula.costo-credito)
├── application-dev.yaml       Oracle local, ddl-auto: update, Swagger habilitado
└── application-prod.yaml      Credenciales por variable de entorno, ddl-auto: validate, Swagger deshabilitado
```

## Decisiones de arquitectura

- **Capas estrictas**: `Controller → Service (interfaz) → ServiceImpl → Repository`. Los controladores nunca reciben ni devuelven entidades, solo DTO; `@Valid` se aplica en el controlador y las reglas de negocio viven exclusivamente en el `*ServiceImpl` correspondiente.
- **`CrudService<REQ, RES, ID>`** (en `service/generic`) define el contrato común (`crear`, `obtenerPorId`, `listar`, `actualizar`, `eliminar`) que implementan `CarreraService` y `CursoService`. `EstudianteService` no lo extiende porque el enunciado (RF-04) no pide `eliminar` para estudiantes.
- **Manejo de errores centralizado**: `GlobalExceptionHandler` traduce `MethodArgumentNotValidException`/`ConstraintViolationException`/`HttpMessageNotReadableException` → 400, `RecursoNoEncontradoException` → 404, `ReglaNegocioException`/`SolicitudInvalidaException` → 409/400 según corresponda, `DataIntegrityViolationException` → 409 (red de seguridad ante restricciones de la base de datos) y cualquier otra excepción → 500 sin exponer la traza. Todas las respuestas de error usan `ErrorResponseDTO`.
- **Auditoría**: `Auditable` (`@MappedSuperclass`) centraliza `fecha_creacion`/`fecha_modificacion` con `@PrePersist`/`@PreUpdate`, heredado por las cinco entidades.
- **Cabecera-detalle transaccional**: `Matricula` usa `@OneToMany(mappedBy = "matricula", cascade = ALL, orphanRemoval = true)` y expone `agregarDetalle(...)` para mantener la relación bidireccional. `MatriculaServiceImpl.registrar(...)` es un único método `@Transactional`: si cualquiera de las reglas RN-01 a RN-04 falla, no se persiste ni la cabecera ni los detalles ni cambios de vacantes (atomicidad real, verificada contra Oracle).
- **Por qué el detalle copia créditos y costo**: si más adelante cambian los créditos de un curso o el costo por crédito, las matrículas ya registradas deben conservar lo que se cobró en su momento. Por eso `DetalleMatricula` guarda su propia copia de `creditos` y `costo` en vez de leer siempre `Curso`.
- **Búsqueda combinable (RF-06)**: `CursoRepository` extiende `JpaSpecificationExecutor<Curso>`; `CursoSpecifications` arma predicados independientes (nombre, carrera, ciclo, vacantes) que `CursoServiceImpl.buscar(...)` combina con `Specification.and(...)` solo para los filtros presentes, y valida el campo de orden contra una lista blanca antes de ordenar.
- **Reporte (RF-07)**: `DetalleMatriculaRepository.matriculadosPorCurso(...)` es una única consulta JPQL con `GROUP BY` y una proyección (`MatriculadosPorCursoDTO`, vía `new pe.edu...MatriculadosPorCursoDTO(...)`), nunca un bucle en Java sobre `findAll()`.
- **Lecturas y `open-in-view: false`**: como `spring.jpa.open-in-view` está deshabilitado (correcto para no ocultar problemas de N+1), todo método de servicio que recorre una asociación `LAZY` al construir un DTO de respuesta (`curso.getCarrera()`, `estudiante.getCarrera()`, `matricula.getDetalles()`) está anotado `@Transactional(readOnly = true)`; de lo contrario Hibernate lanza `LazyInitializationException` fuera de sesión.
- **Dinero**: el costo de cada curso matriculado es `creditos × matricula.costo-credito`, con `BigDecimal` en escala 2 y redondeo `HALF_UP`; el monto total es la suma de los costos, también en escala 2.
- **CORS**: `WebConfig` habilita `/api/**` únicamente para `http://localhost:4200` (cualquier otro origen recibe 403 en el preflight).

## Perfiles

| Perfil | Base de datos | `ddl-auto` | Swagger | Uso |
|---|---|---|---|---|
| `dev` (por defecto) | Oracle local (`application-dev.yaml`, credenciales fijas) | `update` | habilitado en `/swagger-ui.html` | desarrollo local; `DataSeeder` carga los datos semilla si las tablas están vacías |
| `prod` | Oracle vía variables de entorno `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `SERVER_PORT` | `validate` (nunca modifica el esquema) | deshabilitado | despliegue |

El perfil activo se controla con `SPRING_PROFILES_ACTIVE` (por defecto `dev`, ver `application.yaml`).

## Requisitos

- Java 21
- Maven (o el wrapper `./mvnw` / `mvnw.cmd` incluido, no requiere instalación aparte)
- Oracle Database accesible en `localhost:1522/FREEPDB1` (perfil `dev`) con el usuario `MATRICULADB`

## Ejecución

```bash
# Perfil dev (por defecto), con Oracle local ya levantado
./mvnw spring-boot:run

# Perfil prod
SPRING_PROFILES_ACTIVE=prod DB_URL=jdbc:oracle:thin:@host:puerto/servicio \
  DB_USERNAME=usuario DB_PASSWORD=clave ./mvnw spring-boot:run
```

Al arrancar en `dev` con las tablas vacías, `DataSeeder` carga automáticamente 3 carreras, 12 cursos y 6 estudiantes (los mismos datos del Anexo A, ver `datos_semilla.sql`). Si prefieres cargarlos manualmente (por ejemplo, tras un `TRUNCATE`), ejecuta `datos_semilla.sql` en SQL Developer una vez que Hibernate haya creado las tablas.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Salud: `GET /api/v1/health`

## Pruebas

```bash
./mvnw test
```

Incluye `MatriculaBackendApplicationTests` (contexto completo contra Oracle), `CarreraControllerTest` (`@WebMvcTest`) y `MatriculaServiceImplTest` (Mockito, cubre RN-01 a RN-04 y el historial de matrículas).

La colección de Postman está en `/postman/MatriculaBackend.postman_collection.json`, organizada por requerimiento (RF-01 a RF-07) más una carpeta **Anexo B - Casos de prueba oficiales (CP-01 a CP-14)** que reproduce exactamente los casos de la Parte I. Antes de ejecutarla, corre la carpeta **00 Resolver IDs** (siempre primero) para que la colección resuelva los IDs reales de carreras/estudiantes/cursos de tu base, en vez de asumirlos fijos.

```bash
npx newman run postman/MatriculaBackend.postman_collection.json
```

## Convención de ramas y commits

El repositorio sigue el flujo `main` / `develop` / `feature/<funcionalidad>-<apellido>` / `fix/<descripción>-<apellido>` / `sc-<letra>-<apellido>`, con fusiones `--no-ff` hacia `develop` y de `develop` hacia `main`. Los commits usan prefijos `feat`, `fix`, `refactor`, `test`, `docs` en español, describiendo el efecto del cambio. El tag `v1.0-unidad1` marca el commit evaluado de la Unidad 1.
