package com.nexcaja.repository;

import com.nexcaja.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Producto.
 *
 * Permite consultar el catálogo de productos que aparece
 * en la pantalla de caja (caja.html).
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Devuelve todos los productos activos del catálogo.
     * La pantalla de caja llama a este método para mostrar
     * los productos disponibles al cajero.
     *
     * Spring genera: SELECT * FROM producto WHERE activo = true
     *
     * @return lista de productos disponibles para vender
     */
    List<Producto> findByActivoTrue();

    /**
     * Busca productos por categoría (para el filtro de categorías en la caja).
     * Ej: mostrar solo BEBIDAS o solo PERECIBLES.
     *
     * Spring genera: SELECT * FROM producto WHERE categoria = ? AND activo = true
     *
     * @param categoria  la categoría a filtrar (PERECIBLE, BEBIDA, NO_PERECIBLE)
     * @return lista de productos de esa categoría
     */
    List<Producto> findByCategoriaAndActivoTrue(Producto.Categoria categoria);

    /**
     * Busca productos cuyo nombre contenga el texto buscado (búsqueda en la caja).
     * Ignora mayúsculas y minúsculas para mayor comodidad.
     *
     * Spring genera: SELECT * FROM producto WHERE LOWER(nombre) LIKE LOWER('%texto%') AND activo = true
     *
     * @param nombre  texto a buscar dentro del nombre del producto
     * @return lista de productos que coinciden
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Alerta de stock bajo: devuelve productos con pocas unidades.
     * El módulo Smart Refill usa este método para generar alertas.
     *
     * Usamos @Query para escribir JPQL (Java Persistence Query Language),
     * que es SQL pero sobre entidades Java en lugar de tablas.
     *
     * @param umbral  cantidad mínima de stock (ej: 10 unidades)
     * @return lista de productos con stock menor al umbral
     */
    @Query("SELECT p FROM Producto p WHERE p.stock <= :umbral AND p.activo = true ORDER BY p.stock ASC")
    List<Producto> findProductosBajoStock(int umbral);
}
