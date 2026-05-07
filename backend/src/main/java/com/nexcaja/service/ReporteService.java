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
 * Servicio encargado del modulo Smart Refill.
 *
 * Su responsabilidad es analizar las ventas del dia y determinar si
 * el consumo de productos perecibles o bebidas fue inusualmente alto.
 * Si lo fue, genera y guarda alertas en la base de datos para que
 * el administrador las vea en el dashboard al cerrar el turno.
 *
 * Logica de umbrales (valores configurables):
 *   - PERECIBLE: alerta si se vendieron mas de 50 unidades en el turno
 *   - BEBIDA:    alerta si se vendieron mas de 60 unidades en el turno
 *   Si se supera el 50% del umbral extra, la alerta es CRITICA en lugar de PREVENTIVA.
 */
@Service
public class ReporteService {

    // Cuantas unidades de perecibles por turno se consideran "normal"
    private static final int UMBRAL_PERECIBLE = 50;

    // Cuantas unidades de bebidas por turno se consideran "normal"
    private static final int UMBRAL_BEBIDA = 60;

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private AlertaSmartRefillRepository alertaRepository;

    /**
     * Genera el reporte completo de cierre de turno para una fecha dada.
     *
     * Pasos que ejecuta:
     * 1. Obtiene todas las ventas del dia
     * 2. Calcula el total de dinero recaudado
     * 3. Cuenta unidades vendidas por producto y por categoria
     * 4. Detecta si se superaron los umbrales de perecibles y bebidas
     * 5. Crea y guarda en base de datos las alertas necesarias
     * 6. Devuelve un mapa con toda la informacion para mostrar en pantalla
     *
     * @param fecha  El dia del que se quiere el reporte (normalmente la fecha de hoy)
     * @return Mapa con el resumen del dia, incluyendo alertas si las hay
     */
    @Transactional
    public Map<String, Object> generarReporteCierre(LocalDate fecha) {

        LocalDateTime inicioDia = fecha.atStartOfDay();
        LocalDateTime finDia = fecha.atTime(23, 59, 59);

        // Paso 1: traer todas las ventas completadas del dia
        List<Transaccion> ventas = transaccionRepository.findByFechaHoraBetween(inicioDia, finDia);

        // Paso 2: calcular el total de dinero vendido en el dia
        Double totalDinero = transaccionRepository.calcularTotalVentas(inicioDia, finDia);
        if (totalDinero == null) totalDinero = 0.0;

        // Paso 3: contar cuantas unidades se vendieron de cada producto y categoria
        Map<String, Integer> ventasPorProducto = new LinkedHashMap<>();
        Map<String, Integer> ventasPorCategoria = new LinkedHashMap<>();

        for (Transaccion venta : ventas) {
            if (venta.getDetalles() == null) continue;
            for (DetalleTransaccion detalle : venta.getDetalles()) {
                String nombreProducto = detalle.getProducto().getNombre();
                String nombreCategoria = detalle.getProducto().getCategoria().name();
                int cantidad = detalle.getCantidad();

                // Acumulamos por nombre de producto
                ventasPorProducto.merge(nombreProducto, cantidad, Integer::sum);

                // Acumulamos por categoria
                ventasPorCategoria.merge(nombreCategoria, cantidad, Integer::sum);
            }
        }

        // Paso 4: revisar umbrales y construir lista de alertas
        List<AlertaSmartRefill> alertasGeneradas = new ArrayList<>();

        int totalPerecibles = ventasPorCategoria.getOrDefault("PERECIBLE", 0);
        int totalBebidas    = ventasPorCategoria.getOrDefault("BEBIDA", 0);

        // Evaluamos si los perecibles superaron su umbral
        if (totalPerecibles > UMBRAL_PERECIBLE) {
            AlertaSmartRefill alerta = construirAlerta(
                    Producto.Categoria.PERECIBLE,
                    totalPerecibles,
                    UMBRAL_PERECIBLE
            );
            alertasGeneradas.add(alerta);
        }

        // Evaluamos si las bebidas superaron su umbral
        if (totalBebidas > UMBRAL_BEBIDA) {
            AlertaSmartRefill alerta = construirAlerta(
                    Producto.Categoria.BEBIDA,
                    totalBebidas,
                    UMBRAL_BEBIDA
            );
            alertasGeneradas.add(alerta);
        }

        // Paso 5: guardar todas las alertas generadas en la base de datos
        if (!alertasGeneradas.isEmpty()) {
            alertaRepository.saveAll(alertasGeneradas);
        }

        // Construimos la lista de mensajes para mostrar al administrador
        List<String> mensajesAlerta = new ArrayList<>();
        for (AlertaSmartRefill alerta : alertasGeneradas) {
            mensajesAlerta.add("[" + alerta.getNivelAlerta() + "] " + alerta.getMensaje());
        }

        // Paso 6: devolver el reporte completo como mapa
        Map<String, Object> reporte = new LinkedHashMap<>();
        reporte.put("fecha",               fecha.toString());
        reporte.put("totalTransacciones",   ventas.size());
        reporte.put("totalDineroVendido",   totalDinero);
        reporte.put("ventasPorProducto",    ventasPorProducto);
        reporte.put("ventasPorCategoria",   ventasPorCategoria);
        reporte.put("hayAlertas",           !alertasGeneradas.isEmpty());
        reporte.put("alertas",              mensajesAlerta);
        reporte.put("cantidadAlertasPendientes",
                alertaRepository.countByEstadoAlerta(AlertaSmartRefill.EstadoAlerta.PENDIENTE));

        return reporte;
    }

    /**
     * Devuelve todas las alertas que el administrador todavia no ha revisado.
     * Se usa para mostrar el panel de notificaciones en el dashboard.
     */
    public List<AlertaSmartRefill> obtenerAlertasPendientes() {
        return alertaRepository.findByEstadoAlerta(AlertaSmartRefill.EstadoAlerta.PENDIENTE);
    }

    /**
     * Marca una alerta como revisada y guarda la nota del administrador.
     * El administrador llama a esto desde el dashboard cuando confirma
     * que ya tomo las acciones necesarias (por ejemplo, reviso el freezer).
     *
     * @param alertaId ID de la alerta que se va a marcar como revisada
     * @param nota     Comentario opcional del administrador
     * @return La alerta actualizada con estado REVISADA
     */
    @Transactional
    public AlertaSmartRefill marcarAlertaRevisada(Long alertaId, String nota) {
        AlertaSmartRefill alerta = alertaRepository.findById(alertaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontro la alerta con ID: " + alertaId));

        alerta.setEstadoAlerta(AlertaSmartRefill.EstadoAlerta.REVISADA);
        alerta.setNotaAdministrador(nota != null ? nota : "");

        return alertaRepository.save(alerta);
    }

    // ---------------------------------------------------------------
    // Metodos privados de apoyo
    // ---------------------------------------------------------------

    /**
     * Construye el objeto AlertaSmartRefill con todos sus campos calculados.
     * Determina si el nivel es PREVENTIVA o CRITICA segun cuanto se supero el umbral:
     *   - Si el exceso es menor o igual al 50% del umbral -> PREVENTIVA
     *   - Si el exceso supera el 50% del umbral            -> CRITICA
     *
     * @param categoria      Tipo de producto que genero la alerta
     * @param volumenVentas  Cuantas unidades se vendieron realmente
     * @param umbral         Cuantas unidades se consideran normal
     * @return El objeto de alerta listo para guardar en la base de datos
     */
    private AlertaSmartRefill construirAlerta(Producto.Categoria categoria,
                                               int volumenVentas, int umbral) {
        int exceso = volumenVentas - umbral;
        double porcentajeExceso = ((double) exceso / umbral) * 100.0;

        // Si el exceso supera el 50% del umbral, la alerta es critica
        AlertaSmartRefill.NivelAlerta nivel = porcentajeExceso > 50.0
                ? AlertaSmartRefill.NivelAlerta.CRITICA
                : AlertaSmartRefill.NivelAlerta.PREVENTIVA;

        String mensaje = String.format(
                "Alto consumo en %s: %d unidades vendidas (umbral normal: %d). "
                + "Verifique temperatura de equipos de refrigeracion antes del siguiente turno.",
                categoria.name(), volumenVentas, umbral
        );

        AlertaSmartRefill alerta = new AlertaSmartRefill();
        alerta.setFechaHora(LocalDateTime.now());
        alerta.setCategoria(categoria);
        alerta.setVolumenVentas(volumenVentas);
        alerta.setUmbralNormal(umbral);
        alerta.setNivelAlerta(nivel);
        alerta.setEstadoAlerta(AlertaSmartRefill.EstadoAlerta.PENDIENTE);
        alerta.setMensaje(mensaje);

        return alerta;
    }
}
