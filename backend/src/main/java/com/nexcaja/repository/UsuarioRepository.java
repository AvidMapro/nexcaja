package com.nexcaja.repository;

import com.nexcaja.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interfaz que permite al servidor consultar y guardar usuarios
 * en la base de datos. Se usa principalmente para el inicio de sesion.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario.
     * Se usa cuando alguien intenta iniciar sesion: si existe y
     * la contrasena coincide, se le da acceso al sistema.
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si ya existe un usuario con ese nombre de usuario.
     * Se usa al crear un usuario nuevo para evitar duplicados.
     */
    boolean existsByNombreUsuario(String nombreUsuario);
}
