package com.nexcaja.service;

import com.nexcaja.model.Usuario;
import com.nexcaja.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Contiene la logica de negocio para gestionar los usuarios del sistema.
 * Maneja el inicio de sesion y la administracion de cuentas.
 */
@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Verifica si las credenciales de inicio de sesion son correctas.
     * Devuelve el usuario si el nombre y la contrasena coinciden.
     * Devuelve vacio si las credenciales son incorrectas o el usuario esta inactivo.
     */
    public Optional<Usuario> iniciarSesion(String nombreUsuario, String contrasena) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .filter(u -> u.getContrasena().equals(contrasena) && u.getActivo());
    }

    /**
     * Devuelve la lista de todos los usuarios del sistema.
     * Solo el administrador puede ver esta lista.
     */
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca un usuario por su ID.
     */
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Crea un nuevo usuario en el sistema.
     * Verifica primero que el nombre de usuario no este ya en uso.
     * Si ya existe, lanza un error para que el frontend lo informe al administrador.
     */
    public Usuario crear(Usuario usuario) {
        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            throw new IllegalArgumentException(
                "Ya existe un usuario con el nombre: " + usuario.getNombreUsuario()
            );
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Desactiva un usuario para que no pueda iniciar sesion.
     * No lo borra de la base de datos para mantener el historial de ventas.
     */
    public void desactivar(Long id) {
        usuarioRepository.findById(id).ifPresent(usuario -> {
            usuario.setActivo(false);
            usuarioRepository.save(usuario);
        });
    }
}
