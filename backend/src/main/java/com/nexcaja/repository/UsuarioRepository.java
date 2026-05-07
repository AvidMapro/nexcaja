package com.nexcaja.repository;

import com.nexcaja.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la entidad Usuario.
 *
 * Spring Data JPA genera automáticamente la implementación
 * de todos estos métodos — no es necesario escribir SQL.
 *
 * JpaRepository<Usuario, Long> significa:
 *   - Usuario  → la entidad que gestiona este repositorio
 *   - Long     → el tipo del ID (clave primaria) de Usuario
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su nombre de usuario (para el login).
     *
     * Spring genera automáticamente:
     * SELECT * FROM usuario WHERE nombre_usuario = ?
     *
     * Retorna Optional porque el usuario puede no existir.
     *
     * @param nombreUsuario  el nombre de usuario ingresado en el login
     * @return Optional con el usuario si existe, vacío si no
     */
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);

    /**
     * Verifica si ya existe un usuario con ese nombre de usuario.
     * Útil al crear un nuevo usuario para evitar duplicados.
     *
     * Spring genera: SELECT COUNT(*) > 0 FROM usuario WHERE nombre_usuario = ?
     *
     * @param nombreUsuario  nombre de usuario a verificar
     * @return true si ya existe, false si está disponible
     */
    boolean existsByNombreUsuario(String nombreUsuario);

    /**
     * Busca todos los usuarios que están activos en el sistema.
     * Los administradores ven solo usuarios activos en la pantalla de gestión.
     *
     * Spring genera: SELECT * FROM usuario WHERE activo = true
     *
     * @return lista de usuarios activos
     */
    java.util.List<Usuario> findByActivoTrue();
}
