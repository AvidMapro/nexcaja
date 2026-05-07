package com.nexcaja.service;

import com.nexcaja.model.Producto;
import com.nexcaja.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Contiene toda la logica de negocio relacionada con los productos.
 * El controlador recibe la peticion del frontend y llama a este servicio,
 * que es quien decide que hacer con los datos.
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Devuelve todos los productos activos del catalogo.
     * Estos son los que puede ver el cajero en la caja.
     */
    public List<Producto> obtenerProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    /**
     * Devuelve todos los productos (activos e inactivos).
     * Solo el administrador puede ver todos para gestionar el catalogo.
     */
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    /**
     * Busca productos por nombre. Sirve para el buscador manual del cajero.
     * Por ejemplo, si el cajero escribe "emp", devuelve "Empanada de queso" y "Empanada de carne".
     */
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    /**
     * Busca un producto por su ID.
     * Devuelve el producto si existe, o vacio si no se encuentra.
     */
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    /**
     * Guarda un producto nuevo en el catalogo.
     * Si el producto ya existia (tiene ID), lo actualiza.
     */
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    /**
     * Desactiva un producto en lugar de borrarlo.
     * Esto protege el historial de ventas: si borramos el producto,
     * las ventas anteriores que lo incluyeron quedarian sin referencia.
     */
    public void desactivar(Long id) {
        productoRepository.findById(id).ifPresent(producto -> {
            producto.setActivo(false);
            productoRepository.save(producto);
        });
    }

    /**
     * Reduce el stock de un producto cuando se realiza una venta.
     * Si el stock llega a 0, el producto sigue activo pero el sistema
     * puede mostrar una advertencia de que se agoto.
     */
    public void reducirStock(Long productoId, int cantidad) {
        productoRepository.findById(productoId).ifPresent(producto -> {
            int nuevoStock = producto.getStock() - cantidad;
            producto.setStock(Math.max(nuevoStock, 0)); // El stock no puede quedar negativo
            productoRepository.save(producto);
        });
    }
}
