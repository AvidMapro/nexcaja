package com.nexcaja.repository;

import com.nexcaja.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Interfaz que permite al servidor consultar, guardar y eliminar
 * productos de la base de datos sin escribir SQL a mano.
 * Spring genera el codigo necesario automaticamente.
 */
@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    /**
     * Busca todos los productos que esten activos en el catalogo.
     * Los productos inactivos no aparecen en la caja aunque existan en la base de datos.
     */
    List<Producto> findByActivoTrue();

    /**
     * Busca productos cuyo nombre contenga el texto indicado.
     * No distingue entre mayusculas y minusculas.
     * Sirve para el buscador manual del cajero.
     */
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);

    /**
     * Busca todos los productos de una categoria especifica que esten activos.
     * Se usa en el modulo Smart Refill para analizar la rotacion por categoria.
     */
    List<Producto> findByCategoriaAndActivoTrue(Producto.Categoria categoria);
}
