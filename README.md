# 📦 VG-MS-DISTRIBUTION

> **Microservicio de Distribución** — Gestión de programas, rutas y horarios de distribución de agua.

[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3.5](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-brightgreen.svg)](https://www.mongodb.com/atlas)
[![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Messaging-orange.svg)](https://www.rabbitmq.com/)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Capa de Dominio](#-capa-de-dominio)
- [Capa de Aplicación](#-capa-de-aplicación)
- [Capa de Infraestructura](#-capa-de-infraestructura)
- [Endpoints API REST](#-endpoints-api-rest)
- [Eventos RabbitMQ](#-eventos-rabbitmq)
- [Configuración](#-configuración)
- [Variables de Entorno](#-variables-de-entorno)
- [Ejecución](#-ejecución)

---

## 📖 Descripción

`vg-ms-distribution` es un microservicio reactivo que gestiona la distribución de agua potable a través de tres entidades principales:

| Entidad | Descripción |
|---------|-------------|
| **Programa de Distribución** | Planifica la entrega de agua en zonas y calles específicas con fechas y horarios |
| **Ruta de Distribución** | Define el recorrido ordenado por zonas con duración estimada |
| **Horario de Distribución** | Establece los días y horas de distribución por zona |

El microservicio se comunica con otros servicios del ecosistema JASS mediante **RabbitMQ** y se expone a través de un **API Gateway** que gestiona la autenticación.

---

## 🏗️ Arquitectura

El proyecto sigue la **Arquitectura Hexagonal (Ports & Adapters)**, separando responsabilidades en tres capas:

```
┌──────────────────────────────────────────────────────┐
│                    INFRAESTRUCTURA                    │
│  ┌────────────────┐         ┌─────────────────────┐  │
│  │  REST Controllers│        │  MongoDB Repositories│ │
│  │  (Adapters IN)  │        │  (Adapters OUT)      │ │
│  └───────┬────────┘         └──────────┬──────────┘  │
│          │                             │             │
│  ┌───────▼─────────────────────────────▼──────────┐  │
│  │              APLICACIÓN (Use Cases)             │  │
│  │   DTOs · Mappers · Implementaciones Use Cases   │  │
│  └───────┬─────────────────────────────┬──────────┘  │
│          │                             │             │
│  ┌───────▼─────────────────────────────▼──────────┐  │
│  │                   DOMINIO                       │  │
│  │   Models · Ports · Exceptions · Value Objects   │  │
│  └────────────────────────────────────────────────┘  │
│                                                      │
│  ┌──────────────────┐   ┌──────────────────────────┐ │
│  │  RabbitMQ Events  │   │  Security (Gateway)     │ │
│  │  (Adapters OUT)   │   │  (Headers Extractor)    │ │
│  └──────────────────┘   └──────────────────────────┘ │
└──────────────────────────────────────────────────────┘
```

**Principios clave:**
- **Inmutabilidad**: Modelos con `@Getter` + `@Builder(toBuilder = true)`, sin setters
- **Puertos con prefijo `I`**: Interfaces de entrada (Use Cases) y salida (Repositories, EventPublishers)
- **Separación de capas**: El dominio NO depende de infraestructura ni frameworks

---

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| **Java** | 21 | Lenguaje de programación |
| **Spring Boot** | 3.5.10 | Framework base |
| **Spring WebFlux** | - | Stack reactivo (no bloqueante) |
| **Spring Data MongoDB Reactive** | - | Persistencia reactiva |
| **Spring AMQP** | - | Mensajería con RabbitMQ |
| **MongoDB Atlas** | - | Base de datos NoSQL |
| **RabbitMQ** | - | Broker de mensajería para eventos |
| **Lombok** | - | Reducción de boilerplate |
| **SpringDoc OpenAPI** | 2.7.0 | Documentación Swagger |
| **Micrometer Prometheus** | - | Métricas y monitoreo |

---

## 📂 Estructura del Proyecto

```
src/main/java/pe/edu/vallegrande/msdistribution/
│
├── 📁 domain/                          # 🔵 CAPA DE DOMINIO
│   ├── 📁 models/                      # Modelos de dominio (inmutables)
│   │   ├── DistributionProgram.java
│   │   ├── DistributionRoute.java
│   │   ├── DistributionSchedule.java
│   │   └── 📁 valueobjects/
│   │       └── RecordStatus.java       # Enum: ACTIVE, INACTIVE
│   │
│   ├── 📁 exceptions/                  # Excepciones del dominio
│   │   ├── 📁 base/                    # Jerarquía base
│   │   │   ├── DomainException.java    # Clase abstracta base
│   │   │   ├── NotFoundException.java  # HTTP 404
│   │   │   ├── ConflictException.java  # HTTP 409
│   │   │   ├── BusinessRuleException.java  # HTTP 422
│   │   │   └── ValidationException.java    # HTTP 400
│   │   └── 📁 specific/               # Excepciones específicas
│   │       ├── DistributionProgramNotFoundException.java
│   │       ├── DistributionRouteNotFoundException.java
│   │       ├── DistributionScheduleNotFoundException.java
│   │       └── DuplicateProgramCodeException.java
│   │
│   └── 📁 ports/                       # Puertos (interfaces con prefijo I)
│       ├── 📁 in/                      # Puertos de ENTRADA (Use Cases)
│       │   ├── 📁 program/            # 5 use cases: Create, Get, Update, Delete, Restore
│       │   ├── 📁 route/             # 5 use cases
│       │   └── 📁 schedule/          # 5 use cases
│       └── 📁 out/                     # Puertos de SALIDA
│           ├── 📁 program/            # IDistributionProgramRepository + EventPublisher
│           ├── 📁 route/             # IDistributionRouteRepository + EventPublisher
│           ├── 📁 schedule/          # IDistributionScheduleRepository + EventPublisher
│           └── 📁 security/          # ISecurityContext
│
├── 📁 application/                     # 🟢 CAPA DE APLICACIÓN
│   ├── 📁 dto/                         # Data Transfer Objects
│   │   ├── 📁 common/                # ApiResponse<T>, PageResponse<T>, ErrorMessage
│   │   ├── 📁 request/               # DTOs de entrada (Create/Update)
│   │   └── 📁 response/              # DTOs de salida
│   ├── 📁 mappers/                    # Mappers Domain ↔ DTO
│   │   ├── DistributionProgramMapper.java
│   │   ├── DistributionRouteMapper.java
│   │   └── DistributionScheduleMapper.java
│   └── 📁 usecases/                   # Implementaciones de Use Cases
│       ├── 📁 program/               # 5 implementaciones
│       ├── 📁 route/                # 5 implementaciones
│       └── 📁 schedule/             # 5 implementaciones
│
├── 📁 infrastructure/                  # 🟠 CAPA DE INFRAESTRUCTURA
│   ├── 📁 adapters/
│   │   ├── 📁 in/rest/               # Controladores REST
│   │   │   ├── DistributionProgramRest.java
│   │   │   ├── DistributionRouteRest.java
│   │   │   ├── DistributionScheduleRest.java
│   │   │   └── GlobalExceptionHandler.java
│   │   └── 📁 out/
│   │       ├── 📁 persistence/        # Adaptadores de repositorio
│   │       │   ├── DistributionProgramRepositoryImpl.java
│   │       │   ├── DistributionRouteRepositoryImpl.java
│   │       │   └── DistributionScheduleRepositoryImpl.java
│   │       └── 📁 messaging/         # Publicadores de eventos
│   │           ├── DistributionProgramEventPublisherImpl.java
│   │           ├── DistributionRouteEventPublisherImpl.java
│   │           └── DistributionScheduleEventPublisherImpl.java
│   │
│   ├── 📁 persistence/                # Documentos y Repos de MongoDB
│   │   ├── 📁 documents/             # Documentos MongoDB (@Field snake_case)
│   │   │   ├── BaseDocument.java
│   │   │   ├── DistributionProgramDocument.java
│   │   │   ├── DistributionRouteDocument.java
│   │   │   └── DistributionScheduleDocument.java
│   │   └── 📁 repositories/          # Spring Data Reactive Repositories
│   │       ├── DistributionProgramMongoRepository.java
│   │       ├── DistributionRouteMongoRepository.java
│   │       └── DistributionScheduleMongoRepository.java
│   │
│   ├── 📁 config/                      # Configuraciones
│   │   ├── MongoConfig.java           # @EnableReactiveMongoAuditing
│   │   ├── OpenApiConfig.java         # Swagger/OpenAPI
│   │   └── RabbitMQConfig.java        # Exchange jass.events
│   │
│   └── 📁 security/                   # Seguridad vía API Gateway
│       ├── AuthenticatedUser.java     # Modelo de usuario autenticado
│       ├── GatewayHeadersExtractor.java  # Extrae X-User-Id, X-Roles, etc.
│       ├── GatewayHeadersFilter.java  # WebFilter reactivo
│       ├── SecurityConfig.java        # CORS config
│       └── SecurityContextAdapter.java # Implementa ISecurityContext
│
└── VgMsDistribution.java              # Clase principal @SpringBootApplication
```

---

## 🔵 Capa de Dominio

### Modelos de Dominio

Los modelos son **inmutables** — usan `@Getter` + `@Builder(toBuilder = true)` y sus métodos de negocio retornan nuevas instancias:

```java
// Ejemplo: Marcar como eliminado retorna una NUEVA instancia
public DistributionProgram markAsDeleted(String deletedBy) {
    return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
}
```

### Jerarquía de Excepciones

```
DomainException (abstract)
├── NotFoundException       → HTTP 404 (recurso no encontrado)
├── ConflictException       → HTTP 409 (duplicados)
├── BusinessRuleException   → HTTP 422 (reglas de negocio)
└── ValidationException     → HTTP 400 (validación de campos)
```

### Puertos (Interfaces)

Todas las interfaces usan el prefijo `I`:

| Puerto | Tipo | Descripción |
|--------|------|-------------|
| `ICreateDistributionProgramUseCase` | Input | Crear programa |
| `IGetDistributionProgramUseCase` | Input | Consultar programas |
| `IUpdateDistributionProgramUseCase` | Input | Actualizar programa |
| `IDeleteDistributionProgramUseCase` | Input | Desactivar programa (soft delete) |
| `IRestoreDistributionProgramUseCase` | Input | Restaurar programa |
| `IDistributionProgramRepository` | Output | Persistencia |
| `IDistributionProgramEventPublisher` | Output | Publicación de eventos |
| `ISecurityContext` | Output | Contexto de seguridad |

> Lo mismo aplica para `Route` y `Schedule` (5 input ports + 2 output ports cada uno).

---

## 🟢 Capa de Aplicación

### DTOs Comunes

| DTO | Descripción |
|-----|-------------|
| `ApiResponse<T>` | Wrapper estándar para todas las respuestas: `success`, `message`, `data`, `errors`, `timestamp` |
| `PageResponse<T>` | Respuesta paginada: `content`, `page`, `size`, `totalElements`, `totalPages` |
| `ErrorMessage` | Detalle de error: `code`, `message`, `field` |

### Ejemplo de ApiResponse

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2026-02-09T19:45:00"
}
```

### Mappers

Los mappers (`@Component`) convierten entre Domain ↔ DTO:
- `toDomain(CreateRequest)` → Convierte request a modelo de dominio
- `toResponse(DomainModel)` → Convierte modelo de dominio a response DTO

### Use Cases (15 implementaciones)

Cada use case implementa su puerto correspondiente:

```java
@Service
@RequiredArgsConstructor
public class CreateDistributionProgramUseCaseImpl implements ICreateDistributionProgramUseCase {
    private final IDistributionProgramRepository repository;
    private final IDistributionProgramEventPublisher eventPublisher;

    @Override
    public Mono<DistributionProgram> execute(DistributionProgram program, String createdBy) {
        // 1. Verificar duplicados
        // 2. Guardar en MongoDB
        // 3. Publicar evento en RabbitMQ
    }
}
```

---

## 🟠 Capa de Infraestructura

### Documentos MongoDB

Los documentos usan `@Field("snake_case")` para mapeo a MongoDB:

```java
@Document(collection = "program")
@CompoundIndex(def = "{'record_status': 1, 'organization_id': 1}")
public class DistributionProgramDocument extends BaseDocument {
    @Indexed(unique = true)
    @Field("program_code")
    private String programCode;
    // ...
}
```

### Adaptadores de Repositorio

Los `RepositoryImpl` mapean entre **Domain ↔ Document**:

```
IDistributionProgramRepository (puerto dominio)
    └── DistributionProgramRepositoryImpl (@Component)
            └── DistributionProgramMongoRepository (Spring Data)
```

### Seguridad (API Gateway)

La autenticación se gestiona en el **API Gateway**. El microservicio confía en los headers reenviados:

| Header | Descripción |
|--------|-------------|
| `X-User-Id` | ID del usuario autenticado |
| `X-Organization-Id` | ID de la organización |
| `X-User-Email` | Email del usuario |
| `X-Roles` | Roles separados por coma |

El `GatewayHeadersFilter` intercepta todas las peticiones y almacena el usuario en el **contexto reactivo**.

### GlobalExceptionHandler

Maneja automáticamente todas las excepciones del dominio:

| Excepción | HTTP Status | Código |
|-----------|-------------|--------|
| `NotFoundException` | 404 | `ENTITY_NOT_FOUND` |
| `ConflictException` | 409 | `DUPLICATE_ENTITY` |
| `BusinessRuleException` | 422 | `BUSINESS_RULE_VIOLATION` |
| `ValidationException` | 400 | `VALIDATION_ERROR` |
| `WebExchangeBindException` | 400 | `VALIDATION_ERROR` |

---

## 🌐 Endpoints API REST

**Base URL**: `http://localhost:8086`

### Programas de Distribución (`/api/v1/programs`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/programs` | Listar todos los programas |
| `GET` | `/api/v1/programs/active` | Listar programas activos |
| `GET` | `/api/v1/programs/{id}` | Obtener programa por ID |
| `POST` | `/api/v1/programs` | Crear nuevo programa |
| `PUT` | `/api/v1/programs/{id}` | Actualizar programa |
| `PATCH` | `/api/v1/programs/{id}/deactivate` | Desactivar programa (soft delete) |
| `PATCH` | `/api/v1/programs/{id}/restore` | Restaurar programa |

### Rutas de Distribución (`/api/v1/routes`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/routes` | Listar todas las rutas |
| `GET` | `/api/v1/routes/active` | Listar rutas activas |
| `GET` | `/api/v1/routes/{id}` | Obtener ruta por ID |
| `POST` | `/api/v1/routes` | Crear nueva ruta |
| `PUT` | `/api/v1/routes/{id}` | Actualizar ruta |
| `PATCH` | `/api/v1/routes/{id}/deactivate` | Desactivar ruta |
| `PATCH` | `/api/v1/routes/{id}/restore` | Restaurar ruta |

### Horarios de Distribución (`/api/v1/schedules`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/schedules` | Listar todos los horarios |
| `GET` | `/api/v1/schedules/active` | Listar horarios activos |
| `GET` | `/api/v1/schedules/{id}` | Obtener horario por ID |
| `POST` | `/api/v1/schedules` | Crear nuevo horario |
| `PUT` | `/api/v1/schedules/{id}` | Actualizar horario |
| `PATCH` | `/api/v1/schedules/{id}/deactivate` | Desactivar horario |
| `PATCH` | `/api/v1/schedules/{id}/restore` | Restaurar horario |

### Documentación Swagger

- **Swagger UI**: `http://localhost:8086/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8086/v3/api-docs`

---

## 📨 Eventos RabbitMQ

Los eventos se publican en el exchange **`jass.events`** (TopicExchange) con las siguientes routing keys:

### Programa

| Evento | Routing Key | Descripción |
|--------|-------------|-------------|
| Creado | `distribution.program.created` | Programa de distribución creado |
| Actualizado | `distribution.program.updated` | Programa actualizado |
| Eliminado | `distribution.program.deleted` | Programa desactivado |
| Restaurado | `distribution.program.restored` | Programa restaurado |

### Ruta

| Evento | Routing Key |
|--------|-------------|
| Creado | `distribution.route.created` |
| Actualizado | `distribution.route.updated` |
| Eliminado | `distribution.route.deleted` |
| Restaurado | `distribution.route.restored` |

### Horario

| Evento | Routing Key |
|--------|-------------|
| Creado | `distribution.schedule.created` |
| Actualizado | `distribution.schedule.updated` |
| Eliminado | `distribution.schedule.deleted` |
| Restaurado | `distribution.schedule.restored` |

### Estructura del Evento

```json
{
  "eventId": "uuid-v4",
  "correlationId": "uuid-v4",
  "eventType": "PROGRAM_CREATED",
  "entityId": "mongo-object-id",
  "triggeredBy": "user-id",
  "timestamp": "2026-02-09T19:45:00",
  "payload": { ... }
}
```

---

## ⚙️ Configuración

### application.yml (Principal)

```yaml
spring:
  application:
    name: vg-ms-distribution
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: UTC

server:
  port: 8086
```

### application-dev.yml (Desarrollo)

```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://...
      auto-index-creation: true
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: ${RABBITMQ_VHOST:/}
```

### application-prod.yml (Producción)

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: ${RABBITMQ_VHOST:/}
```

---

## 🔑 Variables de Entorno

| Variable | Descripción | Requerida | Default |
|----------|-------------|-----------|---------|
| `MONGODB_URI` | URI de conexión a MongoDB | ✅ (prod) | Atlas en dev |
| `RABBITMQ_HOST` | Host del servidor RabbitMQ | ✅ | - |
| `RABBITMQ_PORT` | Puerto de RabbitMQ | ❌ | `5672` |
| `RABBITMQ_USERNAME` | Usuario de RabbitMQ | ✅ | - |
| `RABBITMQ_PASSWORD` | Contraseña de RabbitMQ | ✅ | - |
| `RABBITMQ_VHOST` | Virtual host de RabbitMQ | ❌ | `/` |

---

## 🚀 Ejecución

### Requisitos Previos

- **Java 21** (JDK)
- **MongoDB** (Atlas o local)
- **RabbitMQ** (local o CloudAMQP)
- **Maven 3.9.6+** (incluido via Maven Wrapper)

### Compilar

```bash
./mvnw clean compile
```

### Ejecutar en Desarrollo

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Ejecutar en Producción

```bash
export MONGODB_URI="mongodb+srv://..."
export RABBITMQ_HOST="rabbit.example.com"
export RABBITMQ_USERNAME="user"
export RABBITMQ_PASSWORD="pass"

./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

### Construir JAR

```bash
./mvnw clean package -DskipTests
java -jar target/vg-ms-distribution-2.0.0.jar --spring.profiles.active=prod
```

### Actuator (Monitoreo)

- **Health**: `http://localhost:8086/actuator/health`
- **Métricas**: `http://localhost:8086/actuator/metrics`
- **Prometheus**: `http://localhost:8086/actuator/prometheus`

---

## 👥 Equipo

**Valle Grande** — Instituto de Educación Superior

---

> Desarrollado siguiendo la **Arquitectura Hexagonal** con patrones de **Domain-Driven Design (DDD)**.
