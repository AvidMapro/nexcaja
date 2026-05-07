/**
 * api.js - Capa de comunicación entre el frontend y el backend NexCaja
 *
 * Este archivo centraliza TODAS las llamadas al servidor.
 * Los HTML solo importan este script y llaman a las funciones de NexCajaAPI.
 *
 * URL base del backend (cambiar si el servidor corre en otro puerto o host)
 */
const API_BASE = 'http://localhost:8080/api';

const NexCajaAPI = {

    // ---------------------------------------------------------------
    // USUARIOS / AUTH
    // ---------------------------------------------------------------

    /**
     * Inicia sesión enviando las credenciales al backend.
     *
     * POST /api/usuarios/login
     * Body: { nombreUsuario, contrasena }
     *
     * Si el login es exitoso, guarda los datos del usuario en
     * sessionStorage para que los otros HTML puedan acceder.
     *
     * @param {string} nombreUsuario
     * @param {string} contrasena
     * @returns {Promise<Object>} datos del usuario autenticado
     * @throws {Error} si las credenciales son incorrectas
     */
    async login(nombreUsuario, contrasena) {
        const res = await fetch(`${API_BASE}/usuarios/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nombreUsuario, contrasena })
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || 'Credenciales incorrectas');
        }
        const usuario = await res.json();
        // Guardar en sesión para que caja.html y dashboard.html lo usen
        sessionStorage.setItem('nexcaja_usuario', JSON.stringify(usuario));
        return usuario;
    },

    /**
     * Devuelve el usuario actualmente autenticado desde sessionStorage.
     * Si no hay sesión activa, redirige al login.
     *
     * @returns {Object} datos del usuario o null
     */
    getUsuarioActual() {
        const raw = sessionStorage.getItem('nexcaja_usuario');
        if (!raw) return null;
        return JSON.parse(raw);
    },

    /**
     * Cierra la sesión borrando los datos del sessionStorage.
     * Redirige al login después de cerrar.
     */
    cerrarSesion() {
        sessionStorage.removeItem('nexcaja_usuario');
        window.location.href = 'login.html';
    },

    // ---------------------------------------------------------------
    // PRODUCTOS
    // ---------------------------------------------------------------

    /**
     * Obtiene el catálogo completo de productos activos.
     * La pantalla de caja llama a este método al cargar.
     *
     * GET /api/productos
     *
     * @returns {Promise<Array>} lista de productos
     */
    async getProductos() {
        const res = await fetch(`${API_BASE}/productos`);
        if (!res.ok) throw new Error('No se pudo cargar el catálogo de productos');
        return res.json();
    },

    /**
     * Busca productos por nombre (búsqueda en tiempo real en la caja).
     *
     * GET /api/productos/buscar?q=empanada
     *
     * @param {string} query  texto a buscar
     * @returns {Promise<Array>} productos que coinciden
     */
    async buscarProductos(query) {
        const res = await fetch(`${API_BASE}/productos/buscar?q=${encodeURIComponent(query)}`);
        if (!res.ok) throw new Error('Error en la búsqueda');
        return res.json();
    },

    // ---------------------------------------------------------------
    // TRANSACCIONES / VENTAS
    // ---------------------------------------------------------------

    /**
     * Registra una venta en el backend.
     * El backend descuenta el stock y guarda la transacción.
     *
     * POST /api/transacciones
     * Body: { cajeroId, metodoPago, montoPagado, items: [{productoId, cantidad}] }
     *
     * @param {Object} venta  objeto con los datos de la venta
     * @returns {Promise<Object>} transacción guardada con su ID
     * @throws {Error} si hay stock insuficiente u otro error
     */
    async registrarVenta(venta) {
        const usuario = this.getUsuarioActual();
        const body = {
            cajeroId:    usuario ? usuario.id : 1,
            metodoPago:  venta.metodoPago,
            montoPagado: venta.montoPagado,
            items:       venta.items
        };
        const res = await fetch(`${API_BASE}/transacciones`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        if (!res.ok) {
            const err = await res.json();
            throw new Error(err.error || 'Error al registrar la venta');
        }
        return res.json();
    },

    // ---------------------------------------------------------------
    // DASHBOARD / REPORTES
    // ---------------------------------------------------------------

    /**
     * Obtiene el reporte de cierre del día indicado.
     * Si no se indica fecha, el backend usa hoy.
     *
     * GET /api/reportes/cierre
     * GET /api/reportes/cierre?fecha=2026-05-07
     *
     * @param {string} [fecha]  fecha en formato YYYY-MM-DD (opcional)
     * @returns {Promise<Object>} reporte completo con KPIs y alertas Smart Refill
     */
    async getReporteCierre(fecha) {
        const url = fecha
            ? `${API_BASE}/reportes/cierre?fecha=${fecha}`
            : `${API_BASE}/reportes/cierre`;
        const res = await fetch(url);
        if (!res.ok) throw new Error('No se pudo cargar el reporte de cierre');
        return res.json();
    },

    /**
     * Devuelve las alertas Smart Refill que aún no han sido revisadas.
     *
     * GET /api/reportes/alertas/pendientes
     *
     * @returns {Promise<Array>} lista de alertas pendientes
     */
    async getAlertasPendientes() {
        const res = await fetch(`${API_BASE}/reportes/alertas/pendientes`);
        if (!res.ok) throw new Error('No se pudieron cargar las alertas');
        return res.json();
    },

    /**
     * Marca una alerta Smart Refill como revisada.
     *
     * PUT /api/reportes/alertas/{id}/revisar
     *
     * @param {number} id    ID de la alerta
     * @param {string} nota  comentario del administrador
     * @returns {Promise<Object>} alerta actualizada
     */
    async marcarAlertaRevisada(id, nota = '') {
        const res = await fetch(`${API_BASE}/reportes/alertas/${id}/revisar`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ nota })
        });
        if (!res.ok) throw new Error('No se pudo marcar la alerta como revisada');
        return res.json();
    },

    // ---------------------------------------------------------------
    // UTILIDADES
    // ---------------------------------------------------------------

    /**
     * Verifica si el backend está disponible.
     * Se llama al cargar cada página para mostrar un aviso si el
     * servidor no está corriendo.
     *
     * @returns {Promise<boolean>} true si el backend responde
     */
    async verificarConexion() {
        try {
            const res = await fetch(`${API_BASE}/productos`, { signal: AbortSignal.timeout(3000) });
            return res.ok;
        } catch {
            return false;
        }
    }
};

// Exportar para uso con módulos ES6 (si se usa en el futuro con bundler)
if (typeof module !== 'undefined') module.exports = NexCajaAPI;
