package com.nexcaja.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un renglon dentro de una venta.
 * Por ejemplo, si en una venta se compraron 2 empanadas y 1 agua,
 * habra dos registros de DetalleTransaccion: uno para cada producto.
 *
 * Se guarda el precio unitario al momento de la venta para que
 * los reportes sean exactos aunque el precio del producto cambie despues.
 */
@Entity
@Table(name = "detalle_transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleTransaccion {

    /** Identificador unico de este renglon */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Transaccion (venta) a la que pertenece este renglon */
    @ManyToOne
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaccion transaccion;

    /** Producto que se vendio en este renglon */
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** Cuantas unidades de este producto se vendieron */
    @Column(nullable = false)
    private Integer cantidad;

    /**
     * Precio del producto en el momento exacto de la venta.
     * Se guarda aqui para que si el administrador cambia el precio
     * del producto mas adelante, el reporte historico siga siendo correcto.
     */
    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    /** Total de este renglon: cantidad multiplicada por el precio unitario */
    @Column(nullable = false)
    private Double subtotal;

    /** Calcula el subtotal automaticamente antes de guardar el detalle */
    @PrePersist
    public void calcularSubtotal() {
        this.subtotal = this.cantidad * this.precioUnitario;
    }
}
