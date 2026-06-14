<div align="center">

# 🍽️ Sabor Mayor

### Plataforma Digital para Restaurante de Alta Experiencia

*Sistema completo de gestión restaurantera construido con arquitectura de microservicios, pensado para escalar desde una sola sede hasta una cadena multisucursal.*

[![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular_21-DD0031?style=for-the-badge&logo=angular&logoColor=white)](https://angular.dev)
[![TypeScript](https://img.shields.io/badge/TypeScript_5.9-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL_16-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Apache Kafka](https://img.shields.io/badge/Apache_Kafka_3.7-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)](https://kafka.apache.org/)

</div>

---

## 📖 Acerca del Proyecto

**Sabor Mayor** es una plataforma digital integral diseñada para un restaurante de alta experiencia gastronómica. El sistema digitaliza y automatiza el ciclo de vida completo del negocio: desde que un cliente realiza una reserva o pedido en línea, hasta que el cocinero lo prepara, el mesero lo sirve y el sistema registra métricas para análisis estratégico.

### Problema que resuelve

Los restaurantes de alta experiencia enfrentan retos operacionales complejos: coordinación en tiempo real entre sala y cocina, gestión de reservas con control de aforo, fidelización de clientes frecuentes y visibilidad instantánea del negocio para administradores. Sabor Mayor resuelve todo esto en una sola plataforma, con distintas interfaces adaptadas al rol de cada usuario.

### Modelo de roles

| Rol | Acceso | Descripción |
|-----|--------|-------------|
| **Cliente** | Web pública | Reservas, pedidos en línea, carrito, seguimiento y perfil |
| **Mesero** | Panel de mesas | Visualización en tiempo real del estado de las mesas y pedidos |
| **Cocinero** | KDS (Kitchen Display) | Tablero de cocina con pedidos activos, tiempos y estados |
| **Admin** | Dashboard de gestión | Control de pedidos, reservas y operación diaria |
| **Super Admin** | Control total | Todo lo anterior más gestión de usuarios y configuración global |

---

## 🏗️ Arquitectura

El proyecto está dividido en dos grandes repositorios que conviven en un monorepo:

```
Sabor_Mayor/
├── sabor-mayor-backend/       # 14 microservicios Java + infraestructura
└── sabor_mayor-frontend-web/  # SPA Angular con SSR
```

### Diagrama de alto nivel

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENTE (Browser)                           │
│                   Angular 21 · SSR · TypeScript                     │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ HTTPS
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                          API GATEWAY :8080                          │
│            JWT Validation · Rate Limiting · CORS · Routing          │
└───┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┘
    │          │          │          │          │          │
    ▼          ▼          ▼          ▼          ▼          ▼
 :8081      :8083      :8084      :8085      :8086      :8087
 Auth     Menu Svc   Order Svc  Reserv.   Payment   Loyalty
 Svc     (Redis ✓)  (WS/STOMP)   Svc       Svc      Points

    │          │          │          │          │          │
    └──────────┴──────────┴──────┬───┴──────────┴──────────┘
                                 │
                         ┌───────▼────────┐
                         │  Apache Kafka  │  Event Bus
                         │  (KRaft mode)  │  orders.events
                         └───────┬────────┘  payments.events
                                 │           reservations.events
              ┌──────────────────┼─────────────────────┐
              ▼                  ▼                      ▼
           :8088              :8089                  :8090
        Notification         Content               Inventory
           Svc               Svc                     Svc
        (Email/SMS/WA)     (Blog/CMS)           (Stock/Recetas)
```

### Principios de diseño

- **Database per service** — Cada microservicio tiene su propia base de datos PostgreSQL aislada
- **Outbox Pattern** — Garantía de entrega de eventos a Kafka sin doble escritura
- **Event-Driven** — Comunicación asíncrona entre servicios mediante topics de Kafka
- **Real-time** — WebSockets con STOMP para el KDS (cocina) y seguimiento de pedidos
- **RBAC** — Control de acceso basado en roles con JWT RS256 firmado y refresh token rotation

---

## 🔧 Stack Tecnológico

### Backend

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Lenguaje | Java | 17 |
| Framework | Spring Boot | 3.3.5 |
| Microservicios | Spring Cloud | 2023.0.3 |
| Seguridad | Spring Security + OAuth2 Resource Server | 6.3.4 |
| Persistencia | Spring Data JPA + Hibernate | 6.5 |
| Migraciones BD | Flyway | 10.x |
| Mensajería | Apache Kafka (KRaft) | 3.7 |
| Caché | Redis 7 | — |
| Service Registry | Netflix Eureka | — |
| Config centralizado | Spring Cloud Config | — |
| Trazabilidad | Micrometer + Zipkin | — |
| API Docs | SpringDoc OpenAPI (Swagger) | 2.6 |
| Build | Maven | 3.9+ |
| Utilidades | Lombok + MapStruct | 1.18 / 1.6 |

### Frontend

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Framework | Angular (Standalone Components) | 21.0 |
| Lenguaje | TypeScript | 5.9 |
| Renderizado | Angular SSR (Express) | — |
| Estilos | SCSS + Variables CSS | — |
| Reactivo | RxJS + Angular Signals | 7.8 |
| Testing | Vitest | 4.0 |
| HTTP | Angular HttpClient + Interceptors | — |

### Infraestructura

| Componente | Tecnología |
|-----------|-----------|
| Base de datos | PostgreSQL 16 (Alpine) |
| Caché y rate limiting | Redis 7 |
| Broker de mensajes | Apache Kafka 3.7 (KRaft, sin Zookeeper) |
| Trazabilidad | Zipkin 3 |
| Contenedores | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## 🛎️ Microservicios

### Plataforma (infraestructura interna)

| Servicio | Puerto | Responsabilidad |
|----------|--------|----------------|
| `discovery-server` | 8761 | Registro de servicios con Netflix Eureka |
| `config-server` | 8888 | Configuración centralizada desde `config-repo/` |
| `api-gateway` | 8080 | Punto de entrada único, validación JWT, rate limiting |

### Servicios de Negocio

| Servicio | Puerto | Responsabilidad |
|----------|--------|----------------|
| `auth-service` | 8081 | Registro, login, JWT (RS256), refresh tokens, RBAC |
| `user-service` | 8082 | Perfiles, direcciones y métodos de pago |
| `menu-service` | 8083 | Platos, categorías, precios; caché en Redis |
| `order-service` | 8084 | Carrito, pedidos, KDS, WebSockets/STOMP, outbox |
| `reservation-service` | 8085 | Reservas, disponibilidad por horario, bloqueo de fechas |
| `payment-service` | 8086 | Procesamiento de pagos (Stripe / Wompi), reembolsos |
| `loyalty-service` | 8087 | Programa Sabor Points, tiers: Alumno → Maestro Chef |
| `notification-service` | 8088 | Correo, SMS, WhatsApp (Twilio) y push notifications |
| `content-service` | 8089 | Blog, galería de fotos, comentarios y SEO |
| `inventory-service` | 8090 | Ingredientes, recetas, control de stock |
| `analytics-service` | 8091 | Dashboards de ventas, reservas y exportación a Excel |

---

## ✨ Funcionalidades Principales

### Para clientes

- **Menú interactivo** con categorías, filtros y búsqueda en tiempo real
- **Carrito de compras** persistente con gestión de cantidades y notas por ítem
- **Pedidos en línea** para comer en el lugar (escaneando QR de la mesa), para llevar o con entrega a domicilio
- **Reservas online** con selección de fecha, hora y tamaño del grupo; depósito requerido para grupos de 8+
- **Seguimiento de pedido** en tiempo real (WebSocket)
- **Programa de fidelidad** Sabor Points con niveles y beneficios acumulables
- **Historial y perfil** con pedidos pasados, reservas y datos personales

### Para personal de sala (Mesero)

- **Panel de mesas** con estado en tiempo real: Libre / Ocupada / Reservada / En mantenimiento
- **Vista de pedidos activos** por mesa con detalle de ítems y totales
- **Actualización de estados** de pedido directamente desde la interfaz

### Para cocina (Cocinero)

- **Kitchen Display System (KDS)** con 3 columnas: Nuevos / En preparación / Listos
- **Tiempos de pedido** con alertas visuales (amarillo 10 min, rojo 20 min+)
- **Auto-refresh** cada 20 segundos sin perder el contexto de la vista

### Para administración (Admin / Super Admin)

- **Dashboard ejecutivo** con métricas de pedidos del día y reservas próximas
- **Gestión de pedidos** con filtros por estado y transiciones de estado manuales
- **Gestión de reservas** sin necesidad de filtrar por fecha específica
- **Gestión de usuarios** (Solo Super Admin) con roles y estado de cuenta
- **Analytics** de ventas, popularidad de platos y ocupación

---

## 📋 Requisitos Previos

- **Docker** 24+ y **Docker Compose** v2
- **Java 17** (solo si se desea compilar localmente)
- **Node.js 22+** y **npm 10+** (solo para desarrollo frontend)
- **Maven 3.9+** (solo para compilar el backend localmente)

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. Clonar el repositorio

```bash
git clone https://github.com/Santcar05/sabor-mayor.git
cd sabor-mayor
```

### 2. Configurar variables de entorno del backend

```bash
cd sabor-mayor-backend
cp .env.example .env
# Editar .env con tus claves (JWT_PRIVATE_KEY_PEM, JWT_PUBLIC_KEY_PEM, etc.)
```

### 3. Compilar los JARs del backend

```bash
# Desde sabor-mayor-backend/
mvn package -DskipTests
```

### 4. Levantar toda la infraestructura con Docker Compose

```bash
# Desde sabor-mayor-backend/
docker compose build --pull=false
docker compose up -d
```

> Los servicios tienen healthchecks y se inician en el orden correcto. El proceso completo tarda ~2 minutos.

### 5. Verificar que todo esté en pie

```bash
docker compose ps
```

Servicios disponibles una vez listos:

| URL | Servicio |
|-----|---------|
| `http://localhost:8080` | API Gateway (punto de entrada) |
| `http://localhost:8761` | Eureka Dashboard |
| `http://localhost:8888` | Config Server |
| `http://localhost:9411` | Zipkin (trazabilidad) |

### 6. Ejecutar el frontend

```bash
cd sabor_mayor-frontend-web
npm install
npm start
# App disponible en http://localhost:4200
```

Para producción con SSR:

```bash
npm run build
npm run serve:ssr:sabor_mayor-frontend
```

---

## 🔐 Seguridad

- **JWT RS256** — Los tokens de acceso se firman con clave privada RSA; cada servicio valida con la clave pública del `auth-service` vía JWKS endpoint
- **Refresh Token Rotation** — Cada uso del refresh token genera uno nuevo (invalidando el anterior)
- **Rate Limiting** — Implementado en el API Gateway con Redis para prevenir abuso
- **RBAC** — 5 roles definidos (`CLIENTE`, `MESERO`, `COCINERO`, `ADMIN`, `SUPER_ADMIN`) con guardas en frontend y `@PreAuthorize` en backend
- **Habeas Data** — Cumplimiento de normativa colombiana de protección de datos personales

---

## 📡 Comunicación entre Servicios

| Tipo | Tecnología | Uso |
|------|-----------|-----|
| Síncrona | HTTP/REST via Feign Client | `order-service` → `menu-service` para validar platos |
| Asíncrona | Apache Kafka | Eventos de dominio entre servicios desacoplados |
| Tiempo real | WebSockets / STOMP | Actualizaciones de pedidos para KDS y seguimiento |

### Topics de Kafka

| Topic | Productor | Consumidores |
|-------|----------|-------------|
| `orders.events` | `order-service` | `loyalty-service`, `notification-service`, `analytics-service` |
| `payments.events` | `payment-service` | `order-service`, `notification-service` |
| `reservations.events` | `reservation-service` | `notification-service`, `analytics-service` |
| `users.events` | `auth-service` | `user-service`, `notification-service` |

---

## 🗄️ Base de Datos

Cada microservicio de negocio tiene su propia base de datos PostgreSQL (aislamiento total):

| Base de Datos | Servicio propietario |
|--------------|---------------------|
| `sabor_auth` | auth-service |
| `sabor_user` | user-service |
| `sabor_menu` | menu-service |
| `sabor_order` | order-service |
| `sabor_reservation` | reservation-service |
| `sabor_payment` | payment-service |
| `sabor_loyalty` | loyalty-service |
| `sabor_notification` | notification-service |
| `sabor_content` | content-service |
| `sabor_inventory` | inventory-service |
| `sabor_analytics` | analytics-service |

Todos corren sobre **una sola instancia de PostgreSQL 16** para facilitar el desarrollo local. Las migraciones de esquema están gestionadas con **Flyway**.

---

## 🧪 Testing

```bash
# Tests unitarios del backend (todos los módulos)
cd sabor-mayor-backend
mvn test

# Tests de integración (requiere Docker para Testcontainers)
mvn verify

# Tests del frontend
cd sabor_mayor-frontend-web
npm test
```

Los tests de integración del backend usan **Testcontainers** para levantar PostgreSQL, Redis y Kafka en contenedores efímeros, garantizando que cada test corra en un entorno limpio.

---

## 📁 Documentación Adicional

La carpeta `sabor-mayor-backend/docs/` contiene documentación técnica detallada:

| Documento | Contenido |
|-----------|-----------|
| `ARCHITECTURE-DECISIONS.md` | ADRs (Architecture Decision Records) |
| `CLASS-DIAGRAM.md` | Diagrama de clases de dominio |
| `FRONTEND-INTEGRATION.md` | Contrato de API para el frontend |
| `INTER-SERVICE-COMMUNICATION.md` | Flujos de comunicación entre servicios |
| `MANUAL-ENDPOINTS.md` | Referencia completa de endpoints REST |

La colección HTTP para pruebas manuales está en `sabor-mayor-backend/http/sabor-mayor.http` (compatible con IntelliJ IDEA HTTP Client y VS Code REST Client).

---

## 🌐 Integraciones Externas

| Integración | Servicio | Propósito |
|-------------|---------|----------|
| **Stripe / Wompi** | payment-service | Procesamiento de pagos y reembolsos |
| **Twilio** | notification-service | SMS y WhatsApp |
| **Google OAuth 2.0** | auth-service | Login social |
| **Google Maps** | frontend | Geocodificación de direcciones de entrega |
| **Claude AI (Anthropic)** | múltiples | Asistente de recomendaciones y automatización |

---

## 🤝 Contribuir

1. Haz fork del repositorio
2. Crea una rama feature: `git checkout -b feature/nombre-funcionalidad`
3. Escribe tus cambios con tests apropiados
4. Verifica que el build pase: `mvn verify`
5. Abre un Pull Request con descripción clara del cambio

---

## 👨‍💻 Autor

**Santiago Castro Garzón**
- GitHub: [@Santcar05](https://github.com/Santcar05)
- Email: castrogarzonsantiago@gmail.com

---

<div align="center">

*Construido con pasión por la gastronomía y la ingeniería de software.*

</div>
