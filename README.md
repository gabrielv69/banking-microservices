# Banking Microservices

Solución de microservicios para gestión bancaria desarrollada con Spring Boot 3.5.10, Spring WebFlux (reactive), R2DBC y PostgreSQL.

## Tabla de Contenidos

- [Descripción](#descripción)
- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [Endpoints Disponibles](#endpoints-disponibles)
- [Testing](#testing)
- [Documentación API](#documentación-api)

---

##  Descripción

Sistema de microservicios bancarios que permite gestionar clientes, cuentas y movimientos financieros. Implementa funcionalidades como:

##  Funcionalidades Implementadas

### F1: CRUD Completo 
- Clientes (Personas)
- Cuentas
- Movimientos

### F2: Registro de Movimientos 
- Tipo DÉBITO: Resta del saldo
- Tipo CRÉDITO: Suma al saldo
- Actualización automática de saldo

### F3: Validación de Saldo 
- Mensaje: "Saldo no disponible"
- HTTP 400 Bad Request
- No registra el movimiento si no hay saldo

### F4: Reporte de Estado de Cuenta 
- Filtro por cliente
- Filtro por rango de fechas
- Incluye: nombre cliente, detalle de cuentas y movimientos

### F5: Pruebas Unitarias 
- 10 tests unitarios
- Mockito + JUnit 5
- Reactor Test (StepVerifier)

### F6: Pruebas de Integración 
- 5 tests de integración
- @SpringBootTest
- Base de datos H2/Test

### F7: Despliegue Docker 
- Dockerfiles multi-stage
- docker-compose.yml
- Health checks
- Redes y volúmenes

---


## Arquitectura

### Microservicios

```
┌──────────────────┐     ┌──────────────────┐
│  customer-service│     │  account-service │
│  Puerto: 8081    │     │  Puerto: 8082    │
└────────┬─────────┘     └────────┬─────────┘
         │                        │
         │                        │
         └────────┬───────────────┘
                  │
         ┌────────▼─────────┐
         │   PostgreSQL     │
         │   Puerto: 5432   │
         └──────────────────┘
```

### Componentes

- **customer-service:** Gestión de clientes (personas)
- **account-service:** Gestión de cuentas, movimientos y reportes
- **PostgreSQL:** Base de datos relacional con dos esquemas (customer_db, account_db)

### Comunicación

- account-service → customer-service (HTTP REST con WebClient)
- Servicios → PostgreSQL (R2DBC reactive)

---

## Tecnologías

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| **Lenguaje** | Java | 21 |
| **Framework** | Spring Boot | 3.5.10 |
| **Reactive** | Spring WebFlux | 6.2.15 |
| **Base de Datos** | PostgreSQL | 15 |
| **ORM** | Spring Data R2DBC | Reactive |
| **API Spec** | OpenAPI | 3.0.0 |
| **Documentación** | Swagger UI | springdoc 2.3.0 |
| **Testing** | JUnit 5, Mockito, Reactor Test | - |
| **Build** | Gradle | 8.5 |
| **Containerización** | Docker, Docker Compose | - |

---

## Requisitos Previos

### Opción 1: Con Docker (Recomendado)

- [Docker](https://www.docker.com/get-started) 20.10+
- [Docker Compose](https://docs.docker.com/compose/install/) 2.0+

### Opción 2: Sin Docker (Desarrollo local)

- Java JDK 21+
- Gradle 8.5+
- PostgreSQL 15+

---

## Instalación y Ejecución

### Opción 1: Docker (Recomendado) 

#### 1. Clonar el repositorio

```bash
git clone <repository-url>
cd banking-microservices
```

#### 2. Levantar todos los servicios

```bash
docker-compose up -d
```

**Esto levanta:**
- PostgreSQL (puerto 5432)
- customer-service (puerto 8081)
- account-service (puerto 8082)

#### 3. Verificar estado de los contenedores

```bash
docker-compose ps
```

**Deberías ver:**
```
NAME                          STATUS
banking-postgres              Up (healthy)
banking-customer-service      Up (healthy)
banking-account-service       Up (healthy)
```

#### 5. Ver logs (opcional)

```bash
# Todos los servicios
docker-compose logs -f

# Un servicio específico
docker-compose logs -f customer-service
docker-compose logs -f account-service
```

#### 6. Detener servicios

```bash
# Detener
docker-compose down

# Detener y eliminar volúmenes (limpia BD)
docker-compose down -v
```

---

### Opción 2: Ejecución Local (Desarrollo)

#### 1. Levantar PostgreSQL

```bash
docker-compose up -d postgres
```

O instalar PostgreSQL localmente y ejecutar el script:
```bash
psql -U postgres -f scripts/BaseDatos.sql
```

#### 2. Ejecutar customer-service

```bash
cd customer-service
./gradlew bootRun
```

#### 3. Ejecutar account-service (en otra terminal)

```bash
cd account-service
./gradlew bootRun
```

---

## Endpoints Disponibles

### Customer Service (Puerto 8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/customers` | Listar todos los clientes |
| GET | `/api/v1/customers/{id}` | Obtener cliente por ID |
| POST | `/api/v1/customers` | Crear nuevo cliente |
| PUT | `/api/v1/customers/{id}` | Actualizar cliente |
| DELETE | `/api/v1/customers/{id}` | Eliminar cliente |

### Account Service (Puerto 8082)

#### Cuentas
| Método | Endpoint                        | Descripción                    |
|--------|---------------------------------|--------------------------------|
| GET | `/api/v1/accounts`              | Listar todas las cuentas       |
| GET | `/api/v1/accounts?customerId=1` | Obtener cuenta por customer ID |
| GET | `/api/v1/accounts/{id}`         | Obtener cuenta por ID          |
| POST | `/api/v1/accounts`              | Crear nueva cuenta             |
| PUT | `/api/v1/accounts/{id}`         | Actualizar cuenta              |
| DELETE | `/api/v1/accounts/{id}`         | Eliminar cuenta                |

#### Movimientos
| Método | Endpoint | Descripción                                  |
|--------|----------|----------------------------------------------|
| GET | `/api/v1/movements` | Listar todos los movimientos                 |
| GET | `/api/v1/movements?customerId=1` | Listar todos los movimientos por customer ID |
| POST | `/api/v1/movements` | Registrar movimiento (F2, F3)                |
| GET | `/api/v1/movements/{id}` | Obtener movimiento por ID                    |
| DELETE | `/api/v1/movements/{id}` | Eliminar movimiento                          |

#### Reportes
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/reports/{customerId}?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD` | Reporte de estado de cuenta (F4) |

### Health Checks

| Servicio | Endpoint |
|----------|----------|
| Customer | `http://localhost:8081/actuator/health` |
| Account | `http://localhost:8082/actuator/health` |

---

##  Testing

### Ejecutar todos los tests

```bash
# Customer service
cd customer-service
./gradlew test

# Account service
cd account-service
./gradlew test
```

### Tests implementados

#### Customer Service
- CustomerServiceTest (5 tests unitarios)

#### Account Service
- **Unitarios:**
    - AccountServiceTest (5 tests)
    - MovementServiceTest (5 tests)
- **Integración:**
    - MovementIntegrationTest (5 tests)

**Total: 15 tests**

### Ver reporte de tests

```bash
./gradlew test
# Reporte en: build/reports/tests/test/index.html
```

---

##  Documentación API

### Swagger UI (Interfaz Interactiva)

Accede a la documentación interactiva:

- **Customer Service:** http://localhost:8081/swagger-ui.html
- **Account Service:** http://localhost:8082/swagger-ui.html

### OpenAPI Spec (JSON)

- **Customer Service:** http://localhost:8081/v3/api-docs
- **Account Service:** http://localhost:8082/v3/api-docs

---




