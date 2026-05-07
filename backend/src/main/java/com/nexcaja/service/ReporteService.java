package com.nexcaja.service;

import com.nexcaja.model.AlertaSmartRefill;
import com.nexcaja.repository.AlertaSmartRefillRepository;
import com.nexcaja.repository.DetalleTransaccionRepository;
import com.nexcaja.repository.TransaccionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Servicio de reportes y Smart Refill.
 *
 * Este servicio hace dos cosas:
 * 1. Genera el reporte de cierre de turno para el dashboard
 * 2. Analiza las ventas para generar alertas Smart Refill automáticamente
 *
 * ¿Qué es Smart Refill?
 * Es un sistema predictivo que analiza el ritmo de ventas por categoría
 * y genera una alerta cuando detecta que una categoría se vende
 * mucho más rápido de lo normal, para que el personal pueda
 * reabastecerse antes de quedarse sin stock.
 */
@Service
public class ReporteService {

    @Autowired private TransaccionRepository transaccionRepository;
    @Autowired private DetalleTransaccionRepository detalleRepository;
    @Autowired private AlertaSmartRefillRepository alertaRepository;

    // Umbral de unidades para considerar que hay un pico de ventas.
    // Si en la última hora se vendieron más de 20 unidades de una categoría,
    // el sistema genera una alerta automáticamente.
    private static final int UMBRAL_PICO = 20;

    // ---------------------------------------------------------------
    // REPORTE DE CIERRE
    // ---------------------------------------------------------------

    /**
     * Genera el reporte completo de cierre para una fecha.
     *
     * Además de calcular los KPIs, este método activa el motor
     * Smart Refill, que analiza las ventas recientes y genera
     * alertas si detecta patrones fuera de lo normal.
     *
     * @param fecha  día del reporte
     * @return mapa con todos los datos del reporte
     */
    public Map<String, Object> generarReporteCierre(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin    = fecha.atTime(LocalTime.MAX);

        // KPIs principales del dashboard
        Double totalVentas        = transaccionRepository.calcularTotalVentas(inicio, fin);
        Long   totalTransacciones = transaccionRepository.contarTransacciones(inicio, fin);
        List<Object[]> topProductos = detalleRepository.topProductosVendidos(inicio, fin);

        double ticketPromedio = (totalTransacciones > 0)
                ? totalVentas / totalTransacciones : 0.0;

        // Top productos formateado para el frontend
        List<Map<String, Object>> ranking = topProductos.stream()
                .map(r -> Map.of("nombre", r[0], "unidades", r[1], "ingresos", r[2]))
                .toList();

        // Ventas por categoría (para el gráfico de dona del dashboard)
        Map<String, Object> ventasPorCategoria = calcularVentasPorCategoria(inicio, fin);

        // Ejecutar el motor Smart Refill y obtener alertas
        List<AlertaSmartRefill> alertasGeneradas = ejecutarSmartRefill();
        List<AlertaSmartRefill> alertasPendientes = alertaRepository.findByResueltaFalse();

        // Construir el reporte final
        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("fecha",                  fecha.toString());
        reporte.put("totalVentas",            Math.round(totalVentas * 100.0) / 100.0);
        reporte.put("totalTransacciones",     totalTransacciones);
        reporte.put("ticketPromedio",         Math.round(ticketPromedio * 100.0) / 100.0);
        reporte.put("topProductos",           ranking);
        reporte.put("ventasPorCategoria",     ventasPorCategoria);
        reporte.put("hayAlertas",             !alertasPendientes.isEmpty());
        reporte.put("cantidadAlertasPendientes", alertasPendientes.size());
        reporte.put("alertasGeneradasAhora",  alertasGeneradas.size());
        return reporte;
    }

    // ---------------------------------------------------------------
    // MOTOR SMART REFILL
    // ---------------------------------------------------------------

    /**
     * Analiza las ventas de la última hora y genera alertas si hay picos.
     *
     * Lógica del algoritmo:
     * 1. Consulta cuántas unidades se vendieron por categoría en la última hora
     * 2. Si alguna categoría supera el umbral (20 unidades), es un pico
     * 3. Verifica que no exista ya una alerta sin resolver para esa categoría
     *    (para no generar la misma alerta cada vez que se llama al reporte)
     * 4. Si es un pico nuevo, guarda la alerta en la base de datos
     *
     * @return lista de alertas nuevas que se generaron en esta ejecución
     */
    private List<AlertaSmartRefill> ejecutarSmartRefill() {
        LocalDateTime ahora         = LocalDateTime.now();
        LocalDateTime desdeHace1hora = ahora.minusHours(1);

        // Consultar picos por categoría en la última hora
        List<Object[]> picos = transaccionRepository
                .detectarPicosPorCategoria(desdeHace1hora, ahora);

        List<AlertaSmartRefill> alertasNuevas = new ArrayList<>();

        for (Object[] pico : picos) {
            String categoria   = pico[0].toString();
            long   unidades    = ((Number) pico[1]).longValue();

            // Solo generar alerta si supera el umbral
            if (unidades >= UMBRAL_PICO) {
                // Verificar que no exista ya una alerta pendiente para esta categoría
                List<AlertaSmartRefill> existentes =
                        alertaRepository.findByCategoriaAndResueltaFalse(categoria);

                if (existentes.isEmpty()) {
                    // ¡Pico detectado! Crear y guardar la alerta
                    AlertaSmartRefill alerta = new AlertaSmartRefill();
                    alerta.setFechaHora(ahora);
                    alerta.setCategoria(categoria);
                    alerta.setTipoAlerta("PICO_VENTAS");
                    alerta.setDescripcion(
                        String.format(
                            "[SMART REFILL] Pico detectado en %s: %d unidades vendidas " +
                            "en la última hora (umbral: %d). Considere reabastecer el exhibidor.",
                            categoria, unidades, UMBRAL_PICO
                        )
                    );
                    alerta.setResuelta(false);
                    alertasNuevas.add(alertaRepository.save(alerta));
                }
            }
        }
        return alertasNuevas;
    }

    // ---------------------------------------------------------------
    // GESTIÓN DE ALERTAS
    // ---------------------------------------------------------------

    /**
     * Devuelve todas las alertas que aún no han sido revisadas.
     * El dashboard las muestra con un badge de urgencia.
     *
     * @return lista de alertas pendientes
     */
    public List<AlertaSmartRefill> obtenerAlertasPendientes() {
        return alertaRepository.findByResueltaFalse();
    }

    /**
     * Marca una alerta como resuelta cuando el administrador la atiende.
     *
     * @param id    ID de la alerta
     * @param nota  comentario del administrador sobre cómo la resolvió
     * @return la alerta actualizada
     * @throws IllegalArgumentException si la alerta no existe
     */
    public AlertaSmartRefill marcarAlertaRevisada(Long id, String nota) {
        AlertaSmartRefill alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe una alerta con ID: " + id));
        alerta.setResuelta(true);
        return alertaRepository.save(alerta);
    }

    // ---------------------------------------------------------------
    // MÉTODO AUXILIAR
    // ---------------------------------------------------------------

    /**
     * Agrupa las ventas por categoría de producto para el gráfico de dona.
     * Recorre todas las transacciones del día y suma unidades por categoría.
     *
     * @param inicio  inicio del día
     * @param fin     fin del día
     * @return mapa { "PERECIBLE": 120, "BEBIDA": 85, "NO_PERECIBLE": 30 }
     */
    private Map<String, Object> calcularVentasPorCategoria(
            LocalDateTime inicio, LocalDateTime fin) {

        List<Object[]> picos = transaccionRepository
                .detectarPicosPorCategoria(inicio, fin);

        Map<String, Object> resultado = new LinkedHashMap<>();
        for (Object[] fila : picos) {
            resultado.put(fila[0].toString(), ((Number) fila[1]).longValue());
        }
        return resultado;
    }
}
