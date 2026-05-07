package com.nexcaja.service;

import com.nexcaja.model.Usuario;
import com.nexcaja.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que contiene la lógica de negocio relacionada con usuarios.
 *
 * La separación en capas funciona así:
 *   Controller → recibe la petición HTTP y delega al Service
 *   Service    → aplica las reglas del negocio (esta clase)
 *   Repository → accede a la base de datos
 *
 * El controlador NO debe saber cómo se valida un login.
 * El repositorio NO debe saber qué hacer si el usuario está inactivo.
 * Esa lógica vive aquí.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Valida las credenciales de inicio de sesión.
     *
     * Reglas de negocio que aplica este método:
     * 1. El usuario debe existir en la base de datos.
     * 2. La contraseña debe coincidir exactamente (en producción se usaría BCrypt).
     * 3. El usuario debe tener el campo 'activo = true'.
     *    Si está inactivo, el login falla aunque la contraseña sea correcta.
     *
     * Retorna Optional para que el controlador decida qué hacer
     * si el login falla (en este caso, devuelve HTTP 401).
     *
     * @param nombreUsuario  nombre de usuario ingresado en el login
     * @param contrasena     contraseña ingresada en el login
     * @return Optional con el usuario autenticado, o vacío si falló
     */
    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        return usuarioRepository
                .findByNombreUsuario(nombreUsuario)
                .filter(u -> u.getContrasena().equals(contrasena)) // Paso 2: verifica contraseña
                .filter(Usuario::getActivo);                        // Paso 3: verifica que esté activo
    }

    /**
     * Devuelve la lista completa de usuarios activos del sistema.
     * El administrador la usa para gestionar cuentas de cajeros.
     *
     * @return lista de usuarios con activo = true
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findByActivoTrue();
    }

    /**
     * Crea un nuevo usuario en el sistema.
     *
     * Regla de negocio: no pueden existir dos usuarios con el mismo
     * nombre de usuario. Si ya existe, lanzamos una excepción que
     * el controlador convierte en un error HTTP 400.
     *
     * @param usuario  objeto con los datos del nuevo usuario
     * @return el usuario guardado con su ID asignado por la base de datos
     * @throws IllegalArgumentException si el nombre de usuario ya está en uso
     */
    public Usuario crear(Usuario usuario) {
        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                "El nombre de usuario '" + usuario.getNombreUsuario() + "' ya está en uso."
            );
        }
        usuario.setActivo(true); // Todo usuario nuevo empieza activo
        return usuarioRepository.save(usuario);
    }

    /**
     * Desactiva un usuario sin borrarlo de la base de datos.
     *
     * Por qué no borramos: si borramos un cajero, perderíamos el historial
     * de todas las ventas que él realizó. Con desactivar es suficiente.
     *
     * @param id  identificador del usuario a desactivar
     */
    public void desactivar(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(false);
            usuarioRepository.save(u);
        });
    }
}
