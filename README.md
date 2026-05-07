# NexCaja — Sistema POS Multimodal

> Sistema de Punto de Venta web con reconocimiento visual y procesamiento de voz para negocios de venta rápida de alimentos.

## Integrantes del equipo

| Nombre | Apellido | Rol en el proyecto |
|--------|----------|--------------------|
| Anthony | Manangon | Backend Java / Integración IA |
| Gabriela | Quiroz | Frontend HTML/JS / Dashboard |
| Zamir | Villalba | Backend Java / Base de datos |

**Universidad Central del Ecuador — Ingeniería de Software**

---

## ¿Qué es NexCaja?

NexCaja es un sistema de caja registradora web que permite al cajero cobrar productos **sin tocar el teclado**, usando:

- 📷 **Cámara web** para identificar los productos colocados frente al dispositivo (visión computacional con YOLOv8)
- 🎤 **Micrófono** para recibir comandos de voz del operador (reconocimiento de voz con Whisper AI)
- 📊 **Dashboard Smart Refill** que al cierre del turno analiza la rotación de inventario y emite alertas preventivas para productos perecibles

---

## Arquitectura del sistema

```
[Navegador del cajero]
        |
   HTTP / WebSocket
        |
[Servidor Java - Spring Boot]  <---HTTP--->  [Microservicio Python - YOLOv8] (Fase 2)
        |
   JPA / SQL
        |
[Base de datos H2 (desarrollo) / PostgreSQL (producción)]
```

---

## Tecnologías utilizadas

| Capa | Tecnología | Versión |
|------|------------|---------|
| Backend | Java + Spring Boot | Java 17, Spring Boot 3.x |
| Base de datos (dev) | H2 In-Memory | - |
| Base de datos (prod) | PostgreSQL | 15+ |
| Frontend | HTML5 + JavaScript puro | - |
| Build tool | Maven | 3.x |
| Control de versiones | Git + GitHub | - |
| IA Visión (Fase 2) | Python + YOLOv8 | Ultralytics 8.x |
| IA Voz (Fase 2) | Whisper AI (WebAssembly) | Modelo Small |

---

## Estructura del repositorio

```
nexcaja/
├── README.md
├── backend/
│   ├── pom.xml                          ← Dependencias Maven
│   └── src/
│       └── main/
│           ├── java/com/nexcaja/
│           │   ├── NexCajaApplication.java        ← Punto de entrada
│           │   ├── controller/                    ← Endpoints REST
│           │   │   ├── ProductoController.java
│           │   │   ├── TransaccionController.java
│           │   │   └── UsuarioController.java
│           │   ├── model/                         ← Entidades de base de datos
│           │   │   ├── Producto.java
│           │   │   ├── Usuario.java
│           │   │   ├── Transaccion.java
│           │   │   └── DetalleTransaccion.java
│           │   ├── repository/                    ← Acceso a datos (JPA)
│           │   │   ├── ProductoRepository.java
│           │   │   ├── TransaccionRepository.java
│           │   │   └── UsuarioRepository.java
│           │   └── service/                       ← Lógica del negocio
│           │       ├── ProductoService.java
│           │       ├── TransaccionService.java
│           │       └── UsuarioService.java
│           └── resources/
│               ├── application.properties         ← Configuración del servidor
│               └── data.sql                       ← Datos de prueba iniciales
└── frontend/
    ├── index.html                                 ← Pantalla de inicio de sesión
    ├── pos.html                                   ← Interfaz del cajero
    ├── admin.html                                 ← Panel del administrador
    ├── css/
    │   └── styles.css                             ← Estilos globales
    └── js/
        ├── auth.js                                ← Lógica de autenticación
        ├── pos.js                                 ← Lógica de la caja registradora
        └── admin.js                               ← Lógica del panel admin
```

---

## Cómo ejecutar el proyecto en local

### Requisitos previos
- Java 17 instalado
- Maven 3.x instalado
- Un navegador moderno (Chrome o Firefox)

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/AvidMapro/nexcaja.git
cd nexcaja

# 2. Ir al directorio del backend
cd backend

# 3. Compilar y ejecutar el servidor
mvn spring-boot:run

# 4. Abrir el frontend en el navegador
# Abrir el archivo frontend/index.html directamente en el navegador
# O acceder a http://localhost:8080 si el servidor sirve los archivos estáticos
```

### Credenciales de prueba

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| cajero1 | nexcaja123 | Cajero |
| admin | admin123 | Administrador |

---

## Estado del desarrollo

- [x] Estructura del proyecto creada
- [x] Modelos de base de datos definidos
- [x] API REST de productos
- [x] API REST de transacciones
- [x] API REST de usuarios
- [x] Frontend: pantalla de login
- [x] Frontend: interfaz POS del cajero
- [x] Frontend: panel de administración
- [ ] Integración con YOLOv8 (Fase 2)
- [ ] Integración con Whisper AI (Fase 2)
- [ ] Dashboard Smart Refill completo
- [ ] Despliegue en producción

---

## Convenciones del código

- Todos los comentarios están escritos en **español, en lenguaje natural**
- Los nombres de clases y métodos están en **inglés** (convención Java)
- Los endpoints REST siguen el estándar **REST** con verbos HTTP correctos
- Cada clase tiene un comentario en la cabecera explicando su propósito
