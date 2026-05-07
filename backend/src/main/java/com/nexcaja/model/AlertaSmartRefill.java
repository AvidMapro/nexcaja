package com.nexcaja.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * Representa una alerta generada por el modulo Smart Refill al cierre de turno.
 *
 * El sistema genera una alerta cuando detecta un volumen de ventas
 * inusualmente alto en productos perecibles o bebidas durante el dia.
 * El objetivo es advertirle al administrador que verifique los equipos
 * de refrigeracion antes de ingresar nuevo inventario.
 *
 * Ejemplo de uso:
 *   - Se vendieron 80 bebidas en el dia (umbral normal es 50)
 *   - El sistema crea una alerta de nivel PREVENTIVA en la categoria BEBIDA
 *   - El administrador la ve en el dashboard y revisa los freezers
 */
@Entity
@Table(name = "alerta_smart_refill")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertaSmartRefill {

    /** Identificador unico de la alerta en la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Fecha y hora exacta en que el sistema genero esta alerta.
     * Se registra automaticamente al cerrar el turno.
     */
    @Column(nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Categoria de producto que disparo la alerta.
     * Solo PERECIBLE y BEBIDA pueden generar alertas,
     * ya que son los que requieren refrigeracion.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Producto.Categoria categoria;

    /**
     * Cuantas unidades de esta categoria se vendieron en el turno.
     * Este valor supero el umbral normal, por eso se genero la alerta.
     */
    @Column(nullable = false)
    private Integer volumenVentas;

    /**
     * Umbral que se supero para generar la alerta.
     * El umbral normal es 50 unidades por turno para perecibles
     * y 60 unidades para bebidas. Se puede ajustar en el futuro.
     */
    @Column(nullable = false)
    private Integer umbralNormal;

    /**
     * Nivel de urgencia de la alerta:
     * PREVENTIVA: el volumen supero el umbral entre 1 y 50 por ciento. Revisar pronto.
     * CRITICA: el volumen supero el umbral mas del 50 por ciento. Revisar inmediatamente.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NivelAlerta nivelAlerta;

    /**
     * Estado actual de la alerta:
     * PENDIENTE: el administrador todavia no la ha revisado.
     * REVISADA: el administrador confirmo que tomo acciones al respecto.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlerta estadoAlerta = EstadoAlerta.PENDIENTE;

    /**
     * Mensaje descriptivo que se muestra al administrador en el dashboard.
     * Ejemplo: "Alto consumo en BEBIDA: 85 unidades vendidas (umbral: 60)"
     */
    @Column(nullable = false, length = 500)
    private String mensaje;

    /**
     * Nota opcional que puede escribir el administrador al marcar la alerta
     * como revisada. Por ejemplo: "Verifique freezer 2, temperatura OK".
     */
    @Column(length = 500)
    private String notaAdministrador;

    // ---------------------------------------------------------------
    // Enumeraciones propias de esta clase
    // ---------------------------------------------------------------

    /** Nivel de urgencia de la alerta generada por Smart Refill */
    public enum NivelAlerta {
        /** El volumen supero el umbral entre 1% y 50% - revisar pronto */
        PREVENTIVA,
        /** El volumen supero el umbral mas del 50% - revisar de inmediato */
        CRITICA
    }

    /** Estado de revision de la alerta por parte del administrador */
    public enum EstadoAlerta {
        /** Todavia no fue revisada por el administrador */
        PENDIENTE,
        /** El administrador confirmo que tomo acciones */
        REVISADA
    }
}
