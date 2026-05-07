-- ============================================================
-- Datos de prueba para NexCaja
-- Universidad Central del Ecuador
--
-- Este archivo se ejecuta automaticamente al arrancar el servidor
-- con la base de datos H2 en memoria.
--
-- ESTRUCTURA:
--   1. Usuarios (cajero y administrador)
--   2. Productos del catalogo
--   3. Transacciones del dia de hoy  <-- activan las alertas Smart Refill
--   4. Detalles de cada transaccion
-- ============================================================


-- ============================================================
-- 1. USUARIOS
-- La contrasena esta en texto plano solo para desarrollo.
-- En produccion se usara BCrypt para cifrarla.
-- ============================================================

INSERT INTO usuario (nombre, apellido, nombre_usuario, contrasena, rol, activo)
VALUES
    ('Carlos',  'Gomez',   'cajero1', 'nexcaja123', 'CAJERO', TRUE),
    ('Admin',   'Sistema', 'admin',   'admin123',   'ADMIN',  TRUE);


-- ============================================================
-- 2. PRODUCTOS DEL CATALOGO
-- ============================================================

INSERT INTO producto (nombre, precio, categoria, stock, activo)
VALUES
    ('Empanada de queso',   0.50, 'PERECIBLE',    50, TRUE),   -- ID 1
    ('Empanada de carne',   0.50, 'PERECIBLE',    50, TRUE),   -- ID 2
    ('Coca-Cola 500ml',     1.00, 'BEBIDA',        30, TRUE),   -- ID 3
    ('Agua sin gas 500ml',  0.50, 'BEBIDA',        40, TRUE),   -- ID 4
    ('Pan de yema',         0.25, 'PERECIBLE',    60, TRUE),   -- ID 5
    ('Yogur de fresa',      0.75, 'PERECIBLE',    20, TRUE),   -- ID 6
    ('Chicles',             0.10, 'NO_PERECIBLE', 100, TRUE),  -- ID 7
    ('Galletas Oreo',       0.50, 'NO_PERECIBLE',  35, TRUE);  -- ID 8


-- ============================================================
-- 3. TRANSACCIONES DEL DIA
--
-- Se insertan 15 ventas del dia de hoy (CURRENT_TIMESTAMP).
-- El total de unidades vendidas sera:
--   PERECIBLE : 57 unidades  --> supera umbral de 50 --> alerta PREVENTIVA
--   BEBIDA    : 73 unidades  --> supera umbral de 60 en mas del 50% --> alerta CRITICA
--   NO_PERECIBLE: 18 unidades --> dentro del rango normal
-- ============================================================

INSERT INTO transaccion (cajero_id, fecha_hora, total, metodo_pago, estado, vuelto)
VALUES
    -- Manana temprano (apertura del local)
    (1, DATEADD('HOUR', -8, CURRENT_TIMESTAMP),  3.50, 'EFECTIVO',  'COMPLETADA', 1.50),  -- T1
    (1, DATEADD('HOUR', -8, CURRENT_TIMESTAMP),  4.00, 'EFECTIVO',  'COMPLETADA', 1.00),  -- T2
    (1, DATEADD('HOUR', -7, CURRENT_TIMESTAMP),  5.25, 'EFECTIVO',  'COMPLETADA', 0.75),  -- T3
    (1, DATEADD('HOUR', -7, CURRENT_TIMESTAMP),  2.50, 'TARJETA',   'COMPLETADA', 0.00),  -- T4

    -- Media manana (pico de ventas de empanadas)
    (1, DATEADD('HOUR', -6, CURRENT_TIMESTAMP),  6.00, 'EFECTIVO',  'COMPLETADA', 4.00),  -- T5
    (1, DATEADD('HOUR', -6, CURRENT_TIMESTAMP),  7.50, 'EFECTIVO',  'COMPLETADA', 2.50),  -- T6
    (1, DATEADD('HOUR', -5, CURRENT_TIMESTAMP),  4.25, 'TARJETA',   'COMPLETADA', 0.00),  -- T7
    (1, DATEADD('HOUR', -5, CURRENT_TIMESTAMP),  8.00, 'EFECTIVO',  'COMPLETADA', 2.00),  -- T8

    -- Tarde (pico de ventas de bebidas)
    (1, DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 10.00, 'EFECTIVO',  'COMPLETADA', 0.00),  -- T9
    (1, DATEADD('HOUR', -4, CURRENT_TIMESTAMP), 12.50, 'TARJETA',   'COMPLETADA', 0.00),  -- T10
    (1, DATEADD('HOUR', -3, CURRENT_TIMESTAMP),  9.00, 'EFECTIVO',  'COMPLETADA', 1.00),  -- T11
    (1, DATEADD('HOUR', -3, CURRENT_TIMESTAMP), 11.00, 'EFECTIVO',  'COMPLETADA', 4.00),  -- T12

    -- Cierre del turno
    (1, DATEADD('HOUR', -2, CURRENT_TIMESTAMP),  6.50, 'TARJETA',   'COMPLETADA', 0.00),  -- T13
    (1, DATEADD('HOUR', -2, CURRENT_TIMESTAMP),  5.00, 'EFECTIVO',  'COMPLETADA', 0.00),  -- T14
    (1, DATEADD('HOUR', -1, CURRENT_TIMESTAMP),  4.75, 'EFECTIVO',  'COMPLETADA', 0.25);  -- T15


-- ============================================================
-- 4. DETALLES DE CADA TRANSACCION
--
-- Cada fila indica cuantas unidades de cada producto se vendieron
-- en una transaccion especifica.
--
-- Conteo final por categoria:
--   PERECIBLE (IDs 1,2,5,6):  4+6+3+4+5+3+1+2+4+3+3+2+1 = 57 unidades
--   BEBIDA    (IDs 3,4):       3+2+4+5+8+7+5+6+8+7+5+5+5+3+1 = 73 unidades
--   NO_PERECIBLE (IDs 7,8):   3+3+3+3+3+3 = 18 unidades
-- ============================================================

INSERT INTO detalle_transaccion (transaccion_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES
    -- T1: 4 empanadas de queso + 3 Coca-Colas
    (1, 1, 4, 0.50, 2.00),
    (1, 3, 3, 1.00, 3.00),

    -- T2: 6 panes de yema + 2 aguas
    (2, 5, 6, 0.25, 1.50),
    (2, 4, 2, 0.50, 1.00),

    -- T3: 3 empanadas de carne + 4 aguas + 3 chicles
    (3, 2, 3, 0.50, 1.50),
    (3, 4, 4, 0.50, 2.00),
    (3, 7, 3, 0.10, 0.30),

    -- T4: 5 Coca-Colas
    (4, 3, 5, 1.00, 5.00),

    -- T5: 4 empanadas de queso + 8 Coca-Colas (pico de bebidas)
    (5, 1, 4, 0.50, 2.00),
    (5, 3, 8, 1.00, 8.00),

    -- T6: 3 yogures + 7 aguas + 3 galletas Oreo
    (6, 6, 3, 0.75, 2.25),
    (6, 4, 7, 0.50, 3.50),
    (6, 8, 3, 0.50, 1.50),

    -- T7: 2 empanadas de queso + 5 Coca-Colas
    (7, 1, 2, 0.50, 1.00),
    (7, 3, 5, 1.00, 5.00),

    -- T8: 5 empanadas de carne + 6 aguas + 3 chicles
    (8, 2, 5, 0.50, 2.50),
    (8, 4, 6, 0.50, 3.00),
    (8, 7, 3, 0.10, 0.30),

    -- T9: 2 yogures + 8 aguas (pico de bebidas tarde)
    (9, 6, 2, 0.75, 1.50),
    (9, 4, 8, 0.50, 4.00),

    -- T10: 3 empanadas de queso + 7 Coca-Colas + 3 galletas
    (10, 1, 3, 0.50, 1.50),
    (10, 3, 7, 1.00, 7.00),
    (10, 8, 3, 0.50, 1.50),

    -- T11: 4 panes de yema + 5 Coca-Colas
    (11, 5, 4, 0.25, 1.00),
    (11, 3, 5, 1.00, 5.00),

    -- T12: 3 empanadas de carne + 5 aguas
    (12, 2, 3, 0.50, 1.50),
    (12, 4, 5, 0.50, 2.50),

    -- T13: 1 yogur + 5 aguas
    (13, 6, 1, 0.75, 0.75),
    (13, 4, 5, 0.50, 2.50),

    -- T14: 2 empanadas de queso + 3 Coca-Colas
    (14, 1, 2, 0.50, 1.00),
    (14, 3, 3, 1.00, 3.00),

    -- T15: 1 pan de yema + 1 Coca-Cola
    (15, 5, 1, 0.25, 0.25),
    (15, 3, 1, 1.00, 1.00);
