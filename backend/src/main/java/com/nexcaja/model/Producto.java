package com.nexcaja.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa un articulo que se vende en el negocio.
 * El catalogo de productos es lo que el cajero puede agregar
 * a una venta, ya sea manualmente, por camara o por voz.
 */
@Entity
@Table(name = "producto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Producto {

    /** Identificador unico del producto en la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre del producto tal como aparece en el sistema, por ejemplo: Empanada de queso */
    @Column(nullable = false)
    private String nombre;

    /** Precio de venta al publico en dolares */
    @Column(nullable = false)
    private Double precio;

    /**
     * Categoria del producto. Es importante para el modulo Smart Refill:
     * los productos PERECIBLES y BEBIDA reciben alertas de temperatura.
     * PERECIBLE: alimentos frescos que se danan facilmente (empanadas, pan, yogur)
     * BEBIDA: bebidas que requieren refrigeracion
     * NO_PERECIBLE: productos con larga vida util (chicles, galletas)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    /** Cantidad de unidades disponibles en el inventario */
    @Column(nullable = false)
    private Integer stock = 0;

    /** Si es false, el producto no aparece en la caja aunque exista en la base de datos */
    @Column(nullable = false)
    private Boolean activo = true;

    /** Enumeracion de las categorias de productos */
    public enum Categoria {
        PERECIBLE,
        BEBIDA,
        NO_PERECIBLE
    }
}
