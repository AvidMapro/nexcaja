-- ============================================================
-- Datos de prueba iniciales para NexCaja
-- Este archivo se ejecuta automaticamente al arrancar el servidor
-- ============================================================

-- Insertar usuarios de prueba
-- La contrasena esta almacenada en texto plano solo para desarrollo.
-- En produccion se usara BCrypt para cifrarla.
INSERT INTO usuario (nombre, apellido, nombre_usuario, contrasena, rol, activo)
VALUES
    ('Carlos', 'Gomez', 'cajero1', 'nexcaja123', 'CAJERO', TRUE),
    ('Admin', 'Sistema', 'admin', 'admin123', 'ADMIN', TRUE);

-- Insertar productos de ejemplo en el catalogo
INSERT INTO producto (nombre, precio, categoria, stock, activo)
VALUES
    ('Empanada de queso', 0.50, 'PERECIBLE', 50, TRUE),
    ('Empanada de carne', 0.50, 'PERECIBLE', 50, TRUE),
    ('Coca-Cola 500ml', 1.00, 'BEBIDA', 30, TRUE),
    ('Agua sin gas 500ml', 0.50, 'BEBIDA', 40, TRUE),
    ('Pan de yema', 0.25, 'PERECIBLE', 60, TRUE),
    ('Yogur de fresa', 0.75, 'PERECIBLE', 20, TRUE),
    ('Chicles', 0.10, 'NO_PERECIBLE', 100, TRUE),
    ('Galletas Oreo', 0.50, 'NO_PERECIBLE', 35, TRUE);
