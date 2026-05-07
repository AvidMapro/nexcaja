package com.nexcaja.service;

import com.nexcaja.model.Producto;
import com.nexcaja.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión del catálogo de productos.
 * Requerido por ProductoController.
 */
@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    /** Devuelve solo los productos activos (los que ve el cajero) */
    public List<Producto> obtenerProductosActivos() {
        return productoRepository.findByActivoTrue();
    }

    /** Devuelve todos los productos incluyendo inactivos (vista admin) */
    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    /** Busca productos por nombre para el buscador en tiempo real */
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre);
    }

    /** Busca producto por ID */
    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    /** Guarda o actualiza un producto */
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    /**
     * Desactiva un producto sin borrarlo.
     * Las ventas históricas siguen apuntando al producto.
     */
    public void desactivar(Long id) {
        productoRepository.findById(id).ifPresent(p -> {
            p.setActivo(false);
            productoRepository.save(p);
        });
    }

    /**
     * Devuelve productos con stock bajo el umbral.
     * Usado por el módulo Smart Refill.
     */
    public List<Producto> obtenerProductosBajoStock(int umbral) {
        return productoRepository.findProductosBajoStock(umbral);
    }
}
