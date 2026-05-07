package com.nexcaja.repository;

import com.nexcaja.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad Transaccion.
 *
 * Contiene las consultas necesarias para:
 * - Guardar ventas desde la caja
 * - Calcular el reporte de cierre del dashboard
 * - Detectar picos de venta para el Smart Refill
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     * Devuelve todas las transacciones completadas en un rango de fechas.
     * El dashboard usa esto para calcular las ventas del turno.
     *
     * @param inicio  fecha y hora de inicio del turno
     * @param fin     fecha y hora de fin del turno
     * @return lista de transacciones del turno
     */
    List<Transaccion> findByFechaHoraBetweenAndEstado(
            LocalDateTime inicio,
            LocalDateTime fin,
            Transaccion.Estado estado
    );

    /**
     * Calcula el total de dinero vendido en un rango de fechas.
     * Resultado directo para el KPI de "Ventas totales" del dashboard.
     *
     * COALESCE devuelve 0 si no hay ventas (evita null).
     *
     * @param inicio  fecha y hora de inicio
     * @param fin     fecha y hora de fin
     * @return suma total de ventas en ese período
     */
    @Query("SELECT COALESCE(SUM(t.total), 0) FROM Transaccion t " +
           "WHERE t.fechaHora BETWEEN :inicio AND :fin " +
           "AND t.estado = 'COMPLETADA'")
    Double calcularTotalVentas(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Cuenta cuántas transacciones se realizaron en un período.
     * KPI de "Transacciones procesadas" del dashboard.
     *
     * @param inicio  fecha y hora de inicio
     * @param fin     fecha y hora de fin
     * @return número de ventas completadas
     */
    @Query("SELECT COUNT(t) FROM Transaccion t " +
           "WHERE t.fechaHora BETWEEN :inicio AND :fin " +
           "AND t.estado = 'COMPLETADA'")
    Long contarTransacciones(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Detecta picos de venta por categoría en la última hora.
     * Este es el CORAZÓN del módulo Smart Refill:
     * compara las ventas de la última hora con el promedio
     * de las 3 horas anteriores para detectar picos.
     *
     * Retorna una lista de Object[] donde:
     *   [0] = categoría del producto (String)
     *   [1] = cantidad vendida en la última hora (Long)
     *
     * @param desdeHace1hora   inicio del período de análisis (última hora)
     * @param ahora            momento actual
     * @return agrupación de ventas por categoría en la última hora
     */
    @Query("SELECT p.categoria, SUM(d.cantidad) " +
           "FROM Transaccion t " +
           "JOIN t.detalles d " +
           "JOIN d.producto p " +
           "WHERE t.fechaHora BETWEEN :desdeHace1hora AND :ahora " +
           "AND t.estado = 'COMPLETADA' " +
           "GROUP BY p.categoria " +
           "ORDER BY SUM(d.cantidad) DESC")
    List<Object[]> detectarPicosPorCategoria(
            LocalDateTime desdeHace1hora,
            LocalDateTime ahora
    );
}
