package com.nexcaja.controller;

import com.nexcaja.model.AlertaSmartRefill;
import com.nexcaja.service.ReporteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el modulo de reportes y Smart Refill.
 * Define los endpoints que el frontend (dashboard del administrador)
 * puede llamar para obtener reportes de cierre y gestionar alertas.
 *
 * Todos los endpoints empiezan con /api/reportes
 */
@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*") // Permite que el frontend HTML llame a estos endpoints
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    // ---------------------------------------------------------------
    // REPORTE DE CIERRE DE TURNO
    // ---------------------------------------------------------------

    /**
     * GET /api/reportes/cierre
     * GET /api/reportes/cierre?fecha=2026-05-07
     *
     * Genera el reporte completo de cierre para una fecha determinada.
     * Si no se indica fecha, usa la fecha de hoy automaticamente.
     *
     * Este endpoint ejecuta toda la logica del Smart Refill:
     * analiza ventas, evalua umbrales y guarda alertas si es necesario.
     *
     * Respuesta de ejemplo:
     * {
     *   "fecha": "2026-05-07",
     *   "totalTransacciones": 42,
     *   "totalDineroVendido": 185.50,
     *   "ventasPorProducto": { "Empanada de queso": 30, "Coca Cola": 25 },
     *   "ventasPorCategoria": { "PERECIBLE": 55, "BEBIDA": 70, "NO_PERECIBLE": 10 },
     *   "hayAlertas": true,
     *   "alertas": ["[CRITICA] Alto consumo en BEBIDA: 70 unidades..."],
     *   "cantidadAlertasPendientes": 2
     * }
     */
    @GetMapping("/cierre")
    public ResponseEntity<Map<String, Object>> generarReporteCierre(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {

        // Si el frontend no envia fecha, usamos hoy
        LocalDate fechaConsulta = (fecha != null) ? fecha : LocalDate.now();

        Map<String, Object> reporte = reporteService.generarReporteCierre(fechaConsulta);
        return ResponseEntity.ok(reporte);
    }

    // ---------------------------------------------------------------
    // GESTION DE ALERTAS SMART REFILL
    // ---------------------------------------------------------------

    /**
     * GET /api/reportes/alertas/pendientes
     *
     * Devuelve todas las alertas que el administrador todavia no ha revisado.
     * El dashboard las muestra con un indicador visual de urgencia
     * para que el administrador sepa cuales requieren atencion inmediata.
     *
     * Respuesta: lista de objetos AlertaSmartRefill con estado PENDIENTE.
     */
    @GetMapping("/alertas/pendientes")
    public List<AlertaSmartRefill> obtenerAlertasPendientes() {
        return reporteService.obtenerAlertasPendientes();
    }

    /**
     * PUT /api/reportes/alertas/{id}/revisar
     *
     * Marca una alerta como revisada y guarda la nota del administrador.
     * El administrador llama a este endpoint desde el dashboard cuando
     * confirma que ya verifico los equipos de refrigeracion.
     *
     * Cuerpo de la peticion (JSON, ambos campos opcionales):
     * {
     *   "nota": "Verifique freezer 2, temperatura correcta a 4 grados."
     * }
     *
     * Devuelve 404 si la alerta no existe.
     * Devuelve 200 con la alerta actualizada si todo salio bien.
     */
    @PutMapping("/alertas/{id}/revisar")
    public ResponseEntity<AlertaSmartRefill> marcarRevisada(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> cuerpo) {

        // Extraemos la nota del cuerpo del JSON, si viene
        String nota = (cuerpo != null) ? cuerpo.getOrDefault("nota", "") : "";

        try {
            AlertaSmartRefill alertaActualizada = reporteService.marcarAlertaRevisada(id, nota);
            return ResponseEntity.ok(alertaActualizada);
        } catch (IllegalArgumentException e) {
            // La alerta no existe en la base de datos
            return ResponseEntity.notFound().build();
        }
    }
}
