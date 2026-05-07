package com.nexcaja.controller;

import com.nexcaja.model.Usuario;
import com.nexcaja.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestion de usuarios y autenticacion.
 * Maneja el inicio de sesion y la administracion de cuentas.
 *
 * Todos los endpoints empiezan con /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    /**
     * POST /api/usuarios/login
     * Verifica las credenciales de inicio de sesion.
     * El frontend envia el nombre de usuario y la contrasena.
     * El servidor responde con los datos del usuario si son correctas,
     * o con un error 401 si son incorrectas.
     *
     * Ejemplo de cuerpo JSON:
     * { "nombreUsuario": "cajero1", "contrasena": "nexcaja123" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        String nombreUsuario = credenciales.get("nombreUsuario");
        String contrasena = credenciales.get("contrasena");

        return usuarioService.iniciarSesion(nombreUsuario, contrasena)
                .map(usuario -> {
                    // Devolvemos los datos del usuario sin la contrasena por seguridad
                    Map<String, Object> respuesta = Map.of(
                            "id", usuario.getId(),
                            "nombre", usuario.getNombre(),
                            "apellido", usuario.getApellido(),
                            "nombreUsuario", usuario.getNombreUsuario(),
                            "rol", usuario.getRol().name()
                    );
                    return ResponseEntity.ok(respuesta);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario o contrasena incorrectos")));
    }

    /**
     * GET /api/usuarios
     * Devuelve la lista de todos los usuarios del sistema.
     * Solo el administrador puede acceder a este endpoint.
     */
    @GetMapping
    public List<Usuario> obtenerTodos() {
        return usuarioService.obtenerTodos();
    }

    /**
     * POST /api/usuarios
     * Crea un nuevo usuario en el sistema.
     * Si el nombre de usuario ya existe, devuelve un error 400.
     */
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Usuario usuario) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * DELETE /api/usuarios/{id}
     * Desactiva un usuario para que no pueda iniciar sesion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return ResponseEntity.ok().build();
    }
}
