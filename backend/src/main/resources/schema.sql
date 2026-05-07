-- ============================================================
-- NexCaja - Schema de referencia de la base de datos
-- Motor: H2 (desarrollo) | PostgreSQL (producción)
-- Generado automáticamente por Hibernate desde las entidades
-- Este archivo es solo DOCUMENTACIÓN del modelo de datos
-- ============================================================

-- Tabla de usuarios (cajeros y administradores)
CREATE TABLE IF NOT EXISTS usuario (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre           VARCHAR(100) NOT NULL,
    apellido         VARCHAR(100) NOT NULL,
    nombre_usuario   VARCHAR(50)  NOT NULL UNIQUE,
    contrasena       VARCHAR(255) NOT NULL,
    rol              VARCHAR(10)  NOT NULL CHECK (rol IN ('CAJERO','ADMIN')),
    activo           BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Tabla de productos del catálogo
CREATE TABLE IF NOT EXISTS producto (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre     VARCHAR(150) NOT NULL,
    precio     DOUBLE       NOT NULL CHECK (precio >= 0),
    categoria  VARCHAR(20)  NOT NULL CHECK (categoria IN ('PERECIBLE','BEBIDA','NO_PERECIBLE')),
    stock      INTEGER      NOT NULL DEFAULT 0 CHECK (stock >= 0),
    activo     BOOLEAN      NOT NULL DEFAULT TRUE
);

-- Tabla de transacciones (ventas)
CREATE TABLE IF NOT EXISTS transaccion (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora   TIMESTAMP   NOT NULL,
    total        DOUBLE      NOT NULL CHECK (total >= 0),
    metodo_pago  VARCHAR(10) NOT NULL CHECK (metodo_pago IN ('EFECTIVO','TARJETA')),
    vuelto       DOUBLE      NOT NULL DEFAULT 0.0,
    usuario_id   BIGINT      NOT NULL REFERENCES usuario(id),
    estado       VARCHAR(15) NOT NULL DEFAULT 'COMPLETADA' CHECK (estado IN ('COMPLETADA','ANULADA'))
);

-- Tabla de detalle de cada transacción (qué productos se vendieron)
CREATE TABLE IF NOT EXISTS detalle_transaccion (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaccion_id  BIGINT  NOT NULL REFERENCES transaccion(id) ON DELETE CASCADE,
    producto_id     BIGINT  NOT NULL REFERENCES producto(id),
    cantidad        INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DOUBLE  NOT NULL CHECK (precio_unitario >= 0),
    subtotal        DOUBLE  NOT NULL CHECK (subtotal >= 0)
);

-- Tabla de alertas Smart Refill
CREATE TABLE IF NOT EXISTS alerta_smart_refill (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha_hora      TIMESTAMP    NOT NULL,
    categoria       VARCHAR(20)  NOT NULL,
    tipo_alerta     VARCHAR(30)  NOT NULL,
    descripcion     VARCHAR(500) NOT NULL,
    resuelta        BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Índices para mejorar el rendimiento de consultas frecuentes
CREATE INDEX IF NOT EXISTS idx_transaccion_fecha    ON transaccion(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_transaccion_usuario  ON transaccion(usuario_id);
CREATE INDEX IF NOT EXISTS idx_detalle_transaccion  ON detalle_transaccion(transaccion_id);
CREATE INDEX IF NOT EXISTS idx_detalle_producto     ON detalle_transaccion(producto_id);
CREATE INDEX IF NOT EXISTS idx_producto_categoria   ON producto(categoria);
