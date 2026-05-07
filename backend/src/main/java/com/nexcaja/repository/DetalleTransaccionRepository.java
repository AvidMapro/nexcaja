package com.nexcaja.repository;

import com.nexcaja.model.DetalleTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio JPA para la entidad DetalleTransaccion.
 *
 * Un "detalle" es un renglón de la venta: por ejemplo,
 * "3 empanadas a $0.50 = $1.50".
 *
 * Este repositorio alimenta el ranking de productos
 * más vendidos del dashboard.
 */
@Repository
public interface DetalleTransaccionRepository extends JpaRepository<DetalleTransaccion, Long> {

    /**
     * Genera el ranking de los productos más vendidos en un período.
     * Alimenta la tabla "Top productos" del dashboard.html.
     *
     * Retorna una lista de Object[] donde cada elemento tiene:
     *   [0] = nombre del producto (String)
     *   [1] = total de unidades vendidas (Long)
     *   [2] = ingresos generados por ese producto (Double)
     *
     * Ordenado de mayor a menor cantidad vendida.
     * LIMIT 10 trae solo el top 10.
     *
     * @param inicio  inicio del período del reporte
     * @param fin     fin del período del reporte
     * @return lista de productos con su cantidad e ingresos
     */
    @Query("SELECT p.nombre, SUM(d.cantidad), SUM(d.subtotal) " +
           "FROM DetalleTransaccion d " +
           "JOIN d.producto p " +
           "JOIN d.transaccion t " +
           "WHERE t.fechaHora BETWEEN :inicio AND :fin " +
           "AND t.estado = 'COMPLETADA' " +
           "GROUP BY p.nombre " +
           "ORDER BY SUM(d.cantidad) DESC " +
           "LIMIT 10")
    List<Object[]> topProductosVendidos(LocalDateTime inicio, LocalDateTime fin);
}
