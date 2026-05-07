package com.nexcaja.controller;

import com.nexcaja.model.Producto;
import com.nexcaja.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controlador REST para la gestion del catalogo de productos.
 * Define los endpoints que el frontend puede llamar para ver,
 * crear, modificar y desactivar productos.
 *
 * Todos los endpoints empiezan con /api/productos
 */
@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite que el frontend HTML llame a estos endpoints
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    /**
     * GET /api/productos
     * Devuelve todos los productos activos del catalogo.
     * El cajero usa este endpoint al cargar la pantalla de caja.
     */
    @GetMapping
    public List<Producto> obtenerProductosActivos() {
        return productoService.obtenerProductosActivos();
    }

    /**
     * GET /api/productos/todos
     * Devuelve todos los productos, incluyendo los inactivos.
     * Solo el administrador usa este endpoint para gestionar el catalogo.
     */
    @GetMapping("/todos")
    public List<Producto> obtenerTodos() {
        return productoService.obtenerTodosLosProductos();
    }

    /**
     * GET /api/productos/buscar?nombre=emp
     * Busca productos por nombre. Sirve para el buscador del cajero.
     * Ejemplo: buscar?nombre=emp devuelve Empanada de queso y Empanada de carne
     */
    @GetMapping("/buscar")
    public List<Producto> buscar(@RequestParam String nombre) {
        return productoService.buscarPorNombre(nombre);
    }

    /**
     * GET /api/productos/{id}
     * Devuelve un producto especifico por su ID.
     * Devuelve 404 si el producto no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return productoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/productos
     * Crea un producto nuevo en el catalogo.
     * El cuerpo de la peticion debe ser un JSON con los datos del producto.
     */
    @PostMapping
    public Producto crear(@RequestBody Producto producto) {
        return productoService.guardar(producto);
    }

    /**
     * PUT /api/productos/{id}
     * Actualiza los datos de un producto existente.
     * Se usa cuando el administrador cambia el precio o el nombre de un producto.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(@PathVariable Long id,
                                                @RequestBody Producto productoActualizado) {
        return productoService.obtenerPorId(id).map(existente -> {
            productoActualizado.setId(id);
            return ResponseEntity.ok(productoService.guardar(productoActualizado));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/productos/{id}
     * Desactiva un producto (no lo borra fisicamente).
     * El producto deja de aparecer en la caja pero su historial se conserva.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        productoService.desactivar(id);
        return ResponseEntity.ok().build();
    }
}
