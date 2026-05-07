package com.nexcaja.config;

import com.nexcaja.model.Producto;
import com.nexcaja.model.Usuario;
import com.nexcaja.repository.ProductoRepository;
import com.nexcaja.repository.UsuarioRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DataLoader — Datos semilla para NexCaja.
 *
 * Se ejecuta UNA sola vez al arrancar el servidor.
 * Carga usuarios y productos de prueba en la base de datos H2
 * para que la caja y el dashboard funcionen desde el primer segundo
 * sin necesidad de ingresar datos manualmente.
 *
 * Para agregar más productos: copiar el patrón de cualquier
 * Producto.builder()...build() y añadirlo a la lista.
 */
@Component
public class DataLoader implements ApplicationRunner {

    private final UsuarioRepository  usuarioRepo;
    private final ProductoRepository productoRepo;

    public DataLoader(UsuarioRepository usuarioRepo,
                      ProductoRepository productoRepo) {
        this.usuarioRepo  = usuarioRepo;
        this.productoRepo = productoRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        cargarUsuarios();
        cargarProductos();
        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║   NexCaja listo  →  http://localhost:8080    ║");
        System.out.println("║   H2 Console     →  /h2-console              ║");
        System.out.println("║   Usuarios cargados: 4                       ║");
        System.out.println("║   Productos cargados: 15                     ║");
        System.out.println("╚══════════════════════════════════════════════╝");
        System.out.println();
    }

    // ----------------------------------------------------------
    // USUARIOS
    // ----------------------------------------------------------
    private void cargarUsuarios() {
        if (usuarioRepo.count() > 0) return; // Ya hay datos, no duplicar

        List<Usuario> usuarios = List.of(
            // Administradores
            Usuario.builder()
                .nombre("Carlos").apellido("Mendoza")
                .nombreUsuario("admin").contrasena("admin123")
                .rol("ADMIN").activo(true)
                .build(),
            Usuario.builder()
                .nombre("Ana").apellido("Torres")
                .nombreUsuario("ana.torres").contrasena("admin123")
                .rol("ADMIN").activo(true)
                .build(),
            // Cajeros
            Usuario.builder()
                .nombre("Juan").apellido("Rodríguez")
                .nombreUsuario("juan.rodriguez").contrasena("caja123")
                .rol("CAJERO").activo(true)
                .build(),
            Usuario.builder()
                .nombre("María").apellido("López")
                .nombreUsuario("maria.lopez").contrasena("caja123")
                .rol("CAJERO").activo(true)
                .build()
        );
        usuarioRepo.saveAll(usuarios);
        System.out.println("[DataLoader] ✔ 4 usuarios cargados");
    }

    // ----------------------------------------------------------
    // PRODUCTOS
    // ----------------------------------------------------------
    private void cargarProductos() {
        if (productoRepo.count() > 0) return; // Ya hay datos, no duplicar

        List<Producto> productos = List.of(

            // --- PERECIBLES ---
            Producto.builder()
                .nombre("Empanada de queso").precio(0.50)
                .categoria("PERECIBLE").stock(45).activo(true)
                .build(),
            Producto.builder()
                .nombre("Empanada de carne").precio(0.60)
                .categoria("PERECIBLE").stock(38).activo(true)
                .build(),
            Producto.builder()
                .nombre("Yogur de fresa 150g").precio(0.75)
                .categoria("PERECIBLE").stock(22).activo(true)
                .build(),
            Producto.builder()
                .nombre("Pan de molde").precio(1.20)
                .categoria("PERECIBLE").stock(15).activo(true)
                .build(),
            Producto.builder()
                .nombre("Sándwich de jamón").precio(1.50)
                .categoria("PERECIBLE").stock(12).activo(true)
                .build(),

            // --- BEBIDAS ---
            Producto.builder()
                .nombre("Coca-Cola 500ml").precio(0.85)
                .categoria("BEBIDA").stock(60).activo(true)
                .build(),
            Producto.builder()
                .nombre("Agua pura 500ml").precio(0.35)
                .categoria("BEBIDA").stock(80).activo(true)
                .build(),
            Producto.builder()
                .nombre("Jugo de naranja 250ml").precio(0.65)
                .categoria("BEBIDA").stock(30).activo(true)
                .build(),
            Producto.builder()
                .nombre("Leche entera 200ml").precio(0.45)
                .categoria("BEBIDA").stock(25).activo(true)
                .build(),
            Producto.builder()
                .nombre("Té frío de durazno").precio(0.70)
                .categoria("BEBIDA").stock(18).activo(true)
                .build(),

            // --- NO PERECIBLES ---
            Producto.builder()
                .nombre("Chicles Trident").precio(0.25)
                .categoria("NO_PERECIBLE").stock(100).activo(true)
                .build(),
            Producto.builder()
                .nombre("Galletas Oreo").precio(0.45)
                .categoria("NO_PERECIBLE").stock(55).activo(true)
                .build(),
            Producto.builder()
                .nombre("Chocolate Snickers").precio(0.80)
                .categoria("NO_PERECIBLE").stock(40).activo(true)
                .build(),
            Producto.builder()
                .nombre("Papas Lays 40g").precio(0.55)
                .categoria("NO_PERECIBLE").stock(48).activo(true)
                .build(),
            Producto.builder()
                .nombre("Gomitas Trolli").precio(0.35)
                .categoria("NO_PERECIBLE").stock(70).activo(true)
                .build()
        );
        productoRepo.saveAll(productos);
        System.out.println("[DataLoader] ✔ 15 productos cargados");
    }
}
