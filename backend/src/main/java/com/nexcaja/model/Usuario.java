package com.nexcaja.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Representa a una persona que puede usar el sistema NexCaja.
 * Puede ser un cajero (que cobra en la caja) o un administrador
 * (que gestiona el catalogo y ve los reportes).
 */
@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    /** Identificador unico de cada usuario en la base de datos */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre real de la persona, por ejemplo: Carlos */
    @Column(nullable = false)
    private String nombre;

    /** Apellido real de la persona, por ejemplo: Gomez */
    @Column(nullable = false)
    private String apellido;

    /** Nombre con el que la persona inicia sesion en el sistema */
    @Column(name = "nombre_usuario", nullable = false, unique = true)
    private String nombreUsuario;

    /** Contrasena de acceso. En produccion se guarda cifrada con BCrypt */
    @Column(nullable = false)
    private String contrasena;

    /**
     * Rol que determina que puede hacer el usuario en el sistema.
     * CAJERO: solo puede cobrar en la caja.
     * ADMIN: puede gestionar productos, ver reportes y administrar usuarios.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    /** Si es false, el usuario no puede iniciar sesion aunque exista en la base de datos */
    @Column(nullable = false)
    private Boolean activo = true;

    /** Enumeracion de los roles posibles del sistema */
    public enum Rol {
        CAJERO,
        ADMIN
    }
}
