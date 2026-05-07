package com.nexcaja.repository;

import com.nexcaja.model.AlertaSmartRefill;
import com.nexcaja.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interfaz que permite al servidor consultar, guardar y actualizar
 * las alertas del modulo Smart Refill en la base de datos.
 * Spring genera el codigo SQL necesario automaticamente a partir
 * del nombre de cada metodo.
 */
@Repository
public interface AlertaSmartRefillRepository extends JpaRepository<AlertaSmartRefill, Long> {

    /**
     * Busca todas las alertas que todavia no fueron revisadas por el administrador.
     * Estas son las que se muestran destacadas en el dashboard al inicio del dia.
     */
    List<AlertaSmartRefill> findByEstadoAlerta(AlertaSmartRefill.EstadoAlerta estadoAlerta);

    /**
     * Busca todas las alertas registradas en un rango de fechas.
     * Sirve para que el administrador consulte el historial de alertas
     * de dias o semanas anteriores.
     */
    List<AlertaSmartRefill> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    /**
     * Busca las alertas de una categoria especifica de producto.
     * Por ejemplo: todas las alertas relacionadas con BEBIDA o PERECIBLE.
     */
    List<AlertaSmartRefill> findByCategoria(Producto.Categoria categoria);

    /**
     * Busca alertas de una categoria que aun no han sido revisadas.
     * Combina los dos filtros anteriores para mostrar lo mas urgente primero.
     */
    List<AlertaSmartRefill> findByCategoriaAndEstadoAlerta(
            Producto.Categoria categoria,
            AlertaSmartRefill.EstadoAlerta estadoAlerta
    );

    /**
     * Busca las alertas de nivel CRITICA que siguen pendientes.
     * Estas son las que requieren atencion inmediata del administrador.
     */
    List<AlertaSmartRefill> findByNivelAlertaAndEstadoAlerta(
            AlertaSmartRefill.NivelAlerta nivelAlerta,
            AlertaSmartRefill.EstadoAlerta estadoAlerta
    );

    /**
     * Cuenta cuantas alertas pendientes existen en total.
     * Se usa para mostrar el contador de notificaciones en la barra del dashboard.
     */
    long countByEstadoAlerta(AlertaSmartRefill.EstadoAlerta estadoAlerta);
}
