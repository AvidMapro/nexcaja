package com.nexcaja.controller;

import com.nexcaja.model.Transaccion;
import com.nexcaja.repository.TransaccionRepository;
import com.nexcaja.service.TransaccionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el registro de ventas y la generacion de reportes.
 * Es el endpoint mas importante del sistema: aqui es donde el cajero
 * confirma cada cobro y donde el administrador obtiene los reportes.
 *
 * Todos los endpoints empiezan con /api/transacciones
 */
@RestController
@RequestMapping("/api/transacciones")
@CrossOrigin(origins = "*")
public class TransaccionController {

    @Autowired
    private TransaccionService transaccionService;

    @Autowired
    private TransaccionRepository transaccionRepository;

    /**
     * POST /api/transacciones
     * Registra una venta nueva en la base de datos.
     * El frontend envia los productos del carrito, el cajero y la forma de pago.
     *
     * Ejemplo de cuerpo JSON:
     * {
     *   "cajeroId": 1,
     *   "metodoPago": "EFECTIVO",
     *   "montoPagado": 5.00,
     *   "items": [
     *     { "productoId": 1, "cantidad": 2 },
     *     { "productoId": 3, "cantidad": 1 }
     *   ]
     * }
     */
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Map<String, Object> datos) {
        try {
            Long cajeroId = Long.parseLong(datos.get("cajeroId").toString());
            String metodoPagoStr = datos.get("metodoPago").toString();
            Double montoPagado = Double.parseDouble(datos.get("montoPagado").toString());
            Transaccion.MetodoPago metodoPago = Transaccion.MetodoPago.valueOf(metodoPagoStr);

            // Convertimos la lista de items del JSON a objetos ItemVenta
            List<Map<String, Object>> itemsJson = (List<Map<String, Object>>) datos.get("items");
            List<TransaccionService.ItemVenta> items = itemsJson.stream().map(item -> {
                TransaccionService.ItemVenta itemVenta = new TransaccionService.ItemVenta();
                itemVenta.setProductoId(Long.parseLong(item.get("productoId").toString()));
                itemVenta.setCantidad(Integer.parseInt(item.get("cantidad").toString()));
                return itemVenta;
            }).toList();

            Transaccion resultado = transaccionService.registrarVenta(
                    cajeroId, items, metodoPago, montoPagado);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/transacciones
     * Devuelve el historial completo de ventas.
     * El administrador lo usa para revisar todas las transacciones.
     */
    @GetMapping
    public List<Transaccion> obtenerTodas() {
        return transaccionRepository.findAll();
    }

    /**
     * GET /api/transacciones/reporte?fecha=2026-05-07
     * Genera el reporte de cierre del dia indicado.
     * Incluye totales, productos mas vendidos y la alerta Smart Refill si corresponde.
     */
    @GetMapping("/reporte")
    public ResponseEntity<Map<String, Object>> generarReporte(
            @RequestParam(required = false) String fecha) {
        try {
            LocalDate fechaReporte = (fecha != null)
                    ? LocalDate.parse(fecha)
                    : LocalDate.now(); // Si no se indica fecha, usa el dia de hoy
            return ResponseEntity.ok(transaccionService.generarReporteDia(fechaReporte));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
