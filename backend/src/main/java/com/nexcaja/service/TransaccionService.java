package com.nexcaja.service;

import com.nexcaja.model.*;
import com.nexcaja.repository.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Servicio que contiene la lógica de negocio de las ventas.
 *
 * Es el servicio más importante del sistema:
 * - Registra cada venta que hace el cajero
 * - Descuenta el stock de cada producto vendido
 * - Genera el reporte de cierre con los KPIs del dashboard
 */
@Service
public class TransaccionService {

    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private DetalleTransaccionRepository detalleRepository;

    // ---------------------------------------------------------------
    // CLASE AUXILIAR: ItemVenta
    // Representa un producto dentro del carrito de compras del cajero
    // ---------------------------------------------------------------

    /**
     * Objeto simple que lleva la info de un producto en el carrito.
     * El controlador convierte el JSON del frontend a esta clase.
     * Ej: { productoId: 2, cantidad: 3 } = 3 Coca-Colas
     */
    @Data
    public static class ItemVenta {
        private Long productoId;  // ID del producto en la base de datos
        private int cantidad;     // Cuántas unidades se venden
    }

    // ---------------------------------------------------------------
    // MÉTODO PRINCIPAL: registrarVenta
    // ---------------------------------------------------------------

    /**
     * Registra una venta completa en la base de datos.
     *
     * Este método hace 4 cosas en una sola operación atómica (@Transactional):
     *   1. Verifica que el cajero y los productos existen
     *   2. Calcula el total sumando precio × cantidad de cada item
     *   3. Descuenta el stock de cada producto vendido
     *   4. Guarda la transacción y sus detalles en la base de datos
     *
     * @Transactional significa que si CUALQUIER paso falla, se deshacen
     * todos los cambios (no queda nada a medias en la base de datos).
     *
     * @param cajeroId     ID del cajero que está cobrando
     * @param items        lista de productos y cantidades del carrito
     * @param metodoPago   EFECTIVO o TARJETA
     * @param montoPagado  dinero que entregó el cliente (para calcular el vuelto)
     * @return la transacción guardada con su ID asignado
     */
    @Transactional
    public Transaccion registrarVenta(Long cajeroId,
                                      List<ItemVenta> items,
                                      Transaccion.MetodoPago metodoPago,
                                      Double montoPagado) {

        // --- Paso 1: Verificar que el cajero existe ---
        Usuario cajero = usuarioRepository.findById(cajeroId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe un cajero con ID: " + cajeroId));

        // --- Paso 2: Calcular el total y preparar los detalles ---
        double total = 0.0;
        List<DetalleTransaccion> detalles = new ArrayList<>();

        for (ItemVenta item : items) {
            // Buscar el producto
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe un producto con ID: " + item.getProductoId()));

            // Verificar que hay suficiente stock
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombre() +
                        "'. Disponible: " + producto.getStock() +
                        ", Solicitado: " + item.getCantidad());
            }

            // Calcular el subtotal de este item
            double subtotal = producto.getPrecio() * item.getCantidad();
            total += subtotal;

            // Crear el detalle de la transacción
            DetalleTransaccion detalle = new DetalleTransaccion();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(subtotal);
            detalles.add(detalle);

            // --- Paso 3: Descontar el stock ---
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        // --- Paso 4: Crear y guardar la transacción ---
        double vuelto = (metodoPago == Transaccion.MetodoPago.EFECTIVO)
                ? Math.max(0.0, montoPagado - total)
                : 0.0;

        Transaccion transaccion = new Transaccion();
        transaccion.setCajero(cajero);
        transaccion.setMetodoPago(metodoPago);
        transaccion.setTotal(total);
        transaccion.setVuelto(vuelto);
        transaccion.setEstado(Transaccion.Estado.COMPLETADA);
        transaccion.setFechaHora(LocalDateTime.now());

        // Guardamos la transacción primero para obtener su ID
        Transaccion guardada = transaccionRepository.save(transaccion);

        // Asociamos los detalles a la transacción guardada
        detalles.forEach(d -> d.setTransaccion(guardada));
        detalleRepository.saveAll(detalles);
        guardada.setDetalles(detalles);

        return guardada;
    }

    // ---------------------------------------------------------------
    // REPORTE DE DÍA
    // ---------------------------------------------------------------

    /**
     * Genera el resumen de ventas de un día completo.
     * Este método alimenta todos los KPIs del dashboard.html:
     * - Total de dinero vendido
     * - Número de transacciones
     * - Ticket promedio (total / transacciones)
     * - Productos más vendidos
     * - Ventas por método de pago
     *
     * @param fecha  el día del cual se quiere el reporte
     * @return mapa con todos los datos del reporte listos para enviar al frontend
     */
    public Map<String, Object> generarReporteDia(LocalDate fecha) {
        // Definir el rango del día: de 00:00:00 a 23:59:59
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.atTime(LocalTime.MAX);

        // Consultas al repositorio
        Double totalVentas = transaccionRepository.calcularTotalVentas(inicio, fin);
        Long totalTransacciones = transaccionRepository.contarTransacciones(inicio, fin);
        List<Object[]> topProductos = detalleRepository.topProductosVendidos(inicio, fin);

        // Ticket promedio = total vendido / número de transacciones
        double ticketPromedio = (totalTransacciones > 0)
                ? totalVentas / totalTransacciones
                : 0.0;

        // Convertir top productos a un formato legible para el frontend
        List<Map<String, Object>> rankingProductos = topProductos.stream()
                .map(row -> Map.of(
                        "nombre",    row[0],
                        "unidades",  row[1],
                        "ingresos",  row[2]
                )).toList();

        // Construir y devolver el reporte
        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("fecha",             fecha.toString());
        reporte.put("totalVentas",       totalVentas);
        reporte.put("totalTransacciones",totalTransacciones);
        reporte.put("ticketPromedio",    Math.round(ticketPromedio * 100.0) / 100.0);
        reporte.put("topProductos",      rankingProductos);
        return reporte;
    }
}
