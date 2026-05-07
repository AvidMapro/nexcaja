package com.nexcaja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal del sistema NexCaja.
 * Al ejecutar esta clase, el servidor arranca en el puerto 8080
 * y queda listo para recibir peticiones del frontend.
 */
@SpringBootApplication
public class NexCajaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexCajaApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   NexCaja - Servidor iniciado");
        System.out.println("   http://localhost:8080");
        System.out.println("   Base de datos: http://localhost:8080/h2-console");
        System.out.println("========================================\n");
    }
}
