package com.nexcaja.repository;

import com.nexcaja.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz que permite al servidor consultar y guardar transacciones
 * (ventas) en la base de datos. Incluye consultas especiales para
 * generar los reportes del modulo Smart Refill.
 */
@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    /**
     * Busca todas las ventas realizadas entre dos fechas.
     * Se usa para generar el reporte de cierre de turno.
     */
    List<Transaccion> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca todas las ventas que hizo un cajero especifico.
     * Se usa en el historial de ventas del panel de administracion.
     */
    List<Transaccion> findByCajeroId(Long cajeroId);

    /**
     * Calcula el total de dinero vendido en un rango de fechas.
     * Retorna la suma de todas las transacciones completadas.
     * Si no hubo ventas, retorna 0.
     */
    @Query("SELECT COALESCE(SUM(t.total), 0) FROM Transaccion t " +
           "WHERE t.fechaHora BETWEEN :inicio AND :fin " +
           "AND t.estado = 'COMPLETADA'")
    Double calcularTotalVentas(@Param("inicio") LocalDateTime inicio,
                               @Param("fin") LocalDateTime fin);
}
