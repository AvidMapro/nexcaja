# NexCaja

Sistema POS académico con backend Java y una interfaz web para gestionar productos, usuarios, ventas y reportes de cierre. El repositorio implementa la API REST y el flujo principal de caja; las integraciones de visión y voz continúan como trabajo futuro.

## Estado actual

- Catálogo de productos con consulta, búsqueda, alta, actualización y baja lógica.
- Registro y consulta de transacciones con validación de existencias.
- Usuarios, inicio de sesión básico y roles de cajero y administrador.
- Reporte de cierre y alertas preventivas de reposición.
- Frontend HTML y JavaScript para login, caja y dashboard.
- Persistencia con PostgreSQL; H2 se utiliza en pruebas.

La configuración actual de Spring Security permite el acceso a los endpoints durante esta fase académica. No debe considerarse una configuración lista para producción.

## Arquitectura

```text
frontend HTML y JavaScript
          |
          | HTTP JSON
          v
Spring Boot REST API
  controller -> service -> repository
          |
          v
PostgreSQL
```

Las integraciones planificadas con YOLOv8 y Whisper no están implementadas en este repositorio.

## Tecnologías verificadas

- Java 17 y Spring Boot 3.2
- Spring Web, Spring Data JPA y Spring Security
- PostgreSQL y H2 para pruebas
- Maven
- HTML y JavaScript

## Equipo y rol

| Integrante | Rol |
| --- | --- |
| Anthony Manangón | Backend Java e integración de IA planificada |
| Gabriela Quiroz | Frontend HTML y JavaScript y dashboard |
| Zamir Villalba | Backend Java y base de datos |

Proyecto universitario de la Universidad Central del Ecuador.

## Estructura

```text
backend/
  pom.xml
  src/main/java/com/nexcaja/
    config/       Configuración y datos iniciales
    controller/   Endpoints REST
    model/        Entidades JPA
    repository/   Acceso a datos
    service/      Lógica de negocio
  src/main/resources/
frontend/
  login.html
  caja.html
  dashboard.html
  js/api.js
```

## Configuración

La aplicación no contiene credenciales versionadas. Define estas variables de entorno antes de iniciarla:

| Variable | Requerida | Propósito |
| --- | --- | --- |
| `DB_URL` | Sí | URL JDBC de PostgreSQL |
| `DB_USERNAME` | Sí | Usuario de la base de datos |
| `DB_PASSWORD` | Sí | Contraseña de la base de datos |
| `SERVER_PORT` | No | Puerto HTTP, `8080` por defecto |
| `JPA_DDL_AUTO` | No | Estrategia de esquema, `update` por defecto |
| `JPA_SHOW_SQL` | No | Muestra SQL, `false` por defecto |
| `DB_POOL_MAX` | No | Máximo de conexiones, `3` por defecto |
| `DB_POOL_MIN` | No | Conexiones inactivas mínimas, `1` por defecto |
| `APP_LOG_LEVEL` | No | Nivel de log de la aplicación |
| `APP_SEED_ENABLED` | No | Activa datos de demostración; `false` por defecto |
| `APP_SEED_PASSWORD` | Condicional | Obligatoria si se activan los usuarios de demostración |

Consulta `.env.example` como referencia. No copies credenciales reales al repositorio.

## Ejecución local

Requisitos: JDK 17, Maven 3 y PostgreSQL.

```bash
git clone https://github.com/AvidMapro/nexcaja.git
cd nexcaja/backend

export DB_URL='jdbc:postgresql://localhost:5432/nexcaja'
export DB_USERNAME='postgres'
export DB_PASSWORD='tu_clave_local'

mvn spring-boot:run
```

Abre `frontend/login.html` desde un servidor estático local. La capa de acceso al backend está centralizada en `frontend/js/api.js` y usa `http://localhost:8080/api`.

## Pruebas

Las pruebas usan H2 y no requieren PostgreSQL ni credenciales externas.

```bash
cd backend
mvn test
```

## Roadmap

- [ ] Proteger contraseñas de usuario con un algoritmo de hash adecuado.
- [ ] Restringir endpoints y completar autenticación para producción.
- [ ] Integrar reconocimiento visual con YOLOv8.
- [ ] Integrar comandos de voz con Whisper.
- [ ] Ampliar las pruebas automatizadas y preparar despliegue.

## Seguridad

Toda conexión externa se configura mediante variables de entorno. Si una credencial fue versionada anteriormente, debe rotarse en el proveedor aunque el historial de Git se haya limpiado, porque clones o forks antiguos pueden conservarla.
