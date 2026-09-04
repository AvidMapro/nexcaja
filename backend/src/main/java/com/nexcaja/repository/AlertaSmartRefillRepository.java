package com.nexcaja.repository;

import com.nexcaja.model.AlertaSmartRefill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad AlertaSmartRefill.
 *
 * Guarda el historial de alertas que el sistema ha generado.
 * Permite al administrador ver qué alertas fueron emitidas
 * durante el turno y cuáles ya fueron atendidas.
 */
@Repository
public interface AlertaSmartRefillRepository extends JpaRepository<AlertaSmartRefill, Long> {

    /**
     * Devuelve las alertas que aún no han sido resueltas.
     * El banner Smart Refill del dashboard muestra estas alertas.
     *
     * Spring genera: SELECT * FROM alerta_smart_refill WHERE resuelta = false
     *
     * @return lista de alertas pendientes de atención
     */
    List<AlertaSmartRefill> findByEstadoAlerta(AlertaSmartRefill.EstadoAlerta estadoAlerta);

    /**
     * Devuelve las alertas de una categoría específica sin resolver.
     * Útil para filtrar: "¿hay alertas de PERECIBLES sin atender?"
     *
     * @param categoria  categoría del producto (PERECIBLE, BEBIDA, NO_PERECIBLE)
     * @return lista de alertas de esa categoría
     */
    List<AlertaSmartRefill> findByCategoriaAndEstadoAlerta(
            com.nexcaja.model.Producto.Categoria categoria,
            AlertaSmartRefill.EstadoAlerta estadoAlerta);
}
