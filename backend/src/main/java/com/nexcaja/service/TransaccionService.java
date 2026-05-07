package com.nexcaja.service;

import com.nexcaja.model.*;
import com.nexcaja.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Contiene la logica de negocio para registrar ventas y generar reportes.
 * Es el servicio mas importante del sistema porque gestiona el dinero.
 */
@Service
public class TransaccionService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registra una venta completa en la base de datos.
     * Este metodo es transaccional: si algo falla en el proceso,
     * todos los cambios se deshacen y la base de datos queda intacta.
     *
     * El proceso es:
     * 1. Verifica que el cajero exista
     * 2. Calcula el total sumando todos los productos
     * 3. Guarda la transaccion
     * 4. Reduce el stock de cada producto vendido
     */
    @Transactional
    public Transaccion registrarVenta(Long cajeroId, List<ItemVenta> items,
                                      Transaccion.MetodoPago metodoPago, Double montoPagado) {

        // Buscamos al cajero que esta realizando la venta
        Usuario cajero = usuarioRepository.findById(cajeroId)
                .orElseThrow(() -> new IllegalArgumentException("Cajero no encontrado con ID: " + cajeroId));

        // Creamos la transaccion nueva
        Transaccion transaccion = new Transaccion();
        transaccion.setCajero(cajero);
        transaccion.setMetodoPago(metodoPago);
        transaccion.setEstado(Transaccion.Estado.COMPLETADA);

        // Construimos los renglones de detalle y calculamos el total
        double total = 0.0;
        List<DetalleTransaccion> detalles = new ArrayList<>();

        for (ItemVenta item : items) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Producto no encontrado con ID: " + item.getProductoId()));

            // Creamos el renglon de detalle para este producto
            DetalleTransaccion detalle = new DetalleTransaccion();
            detalle.setTransaccion(transaccion);
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio()); // Guardamos el precio actual
            detalle.setSubtotal(producto.getPrecio() * item.getCantidad());

            total += detalle.getSubtotal();
            detalles.add(detalle);
        }

        // Asignamos el total y calculamos el vuelto
        transaccion.setTotal(total);
        transaccion.setVuelto(metodoPago == Transaccion.MetodoPago.EFECTIVO
                ? Math.max(montoPagado - total, 0.0)
                : 0.0);
        transaccion.setDetalles(detalles);

        // Guardamos la transaccion (los detalles se guardan automaticamente por cascade)
        Transaccion guardada = transaccionRepository.save(transaccion);

        // Reducimos el stock de cada producto vendido
        for (ItemVenta item : items) {
            // Usamos el servicio de producto para reducir el stock correctamente
            productoRepository.findById(item.getProductoId()).ifPresent(p -> {
                p.setStock(Math.max(p.getStock() - item.getCantidad(), 0));
                productoRepository.save(p);
            });
        }

        return guardada;
    }

    /**
     * Genera el reporte de cierre del dia de hoy.
     * Devuelve un mapa con la informacion que el modulo Smart Refill necesita:
     * - Total de ventas del dia
     * - Cuantas unidades se vendieron de cada producto
     * - Si hay alerta por alto consumo de productos perecibles
     */
    public Map<String, Object> generarReporteDia(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(23, 59, 59);

        // Obtenemos todas las ventas del dia
        List<Transaccion> ventas = transaccionRepository.findByFechaHoraBetween(inicio, fin);
        Double totalDia = transaccionRepository.calcularTotalVentas(inicio, fin);

        // Contamos cuantas unidades se vendieron de cada producto
        Map<String, Integer> ventasPorProducto = new HashMap<>();
        Map<String, Integer> ventasPorCategoria = new HashMap<>();

        for (Transaccion venta : ventas) {
            if (venta.getDetalles() != null) {
                for (DetalleTransaccion detalle : venta.getDetalles()) {
                    String nombreProducto = detalle.getProducto().getNombre();
                    String categoria = detalle.getProducto().getCategoria().name();

                    ventasPorProducto.merge(nombreProducto, detalle.getCantidad(), Integer::sum);
                    ventasPorCategoria.merge(categoria, detalle.getCantidad(), Integer::sum);
                }
            }
        }

        // Revisamos si hay alerta: si se vendieron mas de 30 unidades de perecibles o bebidas
        int ventasPerecibles = ventasPorCategoria.getOrDefault("PERECIBLE", 0);
        int ventasBebidas = ventasPorCategoria.getOrDefault("BEBIDA", 0);
        boolean hayAlerta = ventasPerecibles > 30 || ventasBebidas > 20;

        // Construimos el reporte final
        Map<String, Object> reporte = new HashMap<>();
        reporte.put("fecha", fecha.toString());
        reporte.put("totalTransacciones", ventas.size());
        reporte.put("totalDineroVendido", totalDia);
        reporte.put("ventasPorProducto", ventasPorProducto);
        reporte.put("ventasPorCategoria", ventasPorCategoria);
        reporte.put("alertaSmartRefill", hayAlerta);
        reporte.put("mensajeAlerta", hayAlerta
                ? "ALERTA: Alto consumo de productos perecibles o bebidas detectado. Verifique temperatura de equipos antes del siguiente turno."
                : "Sin alertas. El consumo de productos perecibles esta dentro del rango normal.");

        return reporte;
    }

    /**
     * Clase auxiliar que representa un producto en el carrito de la venta.
     * El frontend envia una lista de estos objetos cuando el cajero confirma el cobro.
     */
    public static class ItemVenta {
        private Long productoId;  // ID del producto
        private Integer cantidad; // Cuantas unidades se vendieron

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    }
}
