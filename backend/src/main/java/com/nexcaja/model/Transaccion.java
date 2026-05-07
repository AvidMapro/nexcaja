package com.nexcaja.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa una venta completa realizada en la caja.
 * Cada vez que el cajero cobra a un cliente y confirma la venta,
 * se crea un registro de Transaccion en la base de datos.
 */
@Entity
@Table(name = "transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    /** Identificador unico de la transaccion */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Fecha y hora exacta en que se realizo la venta */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Suma total de todos los productos vendidos en esta transaccion */
    @Column(nullable = false)
    private Double total;

    /**
     * Forma en que el cliente pago la compra.
     * EFECTIVO: el cliente pago con billetes o monedas.
     * TARJETA: el cliente pago con tarjeta de debito o credito.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    /** Dinero que se le devolvio al cliente si pago con efectivo */
    @Column(nullable = false)
    private Double vuelto = 0.0;

    /** Cajero que atendio esta venta */
    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario cajero;

    /**
     * Lista de todos los productos que se vendieron en esta transaccion.
     * Si se vendieron 3 empanadas y 2 aguas, habra 2 registros de detalle.
     * Al borrar la transaccion, sus detalles tambien se borran (cascade).
     */
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleTransaccion> detalles;

    /** Estado de la transaccion: solo las COMPLETADAS se cuentan en los reportes */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado = Estado.COMPLETADA;

    /** Enumeracion de los metodos de pago aceptados */
    public enum MetodoPago {
        EFECTIVO,
        TARJETA
    }

    /** Enumeracion de los estados posibles de una transaccion */
    public enum Estado {
        COMPLETADA,
        ANULADA
    }

    /** Se asigna automaticamente la fecha y hora actual antes de guardar */
    @PrePersist
    public void asignarFechaHora() {
        this.fechaHora = LocalDateTime.now();
    }
}
