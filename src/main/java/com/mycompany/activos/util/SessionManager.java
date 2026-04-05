package com.mycompany.activos.util;

import com.mycompany.activos.model.Usuario;

public class SessionManager {

    private static SessionManager instancia;
    private Usuario usuarioActual;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean esAdmin() {
        return usuarioActual != null && "admin".equalsIgnoreCase(usuarioActual.getRol());
    }

    public boolean esAnalista() {
        return usuarioActual != null && "analista".equalsIgnoreCase(usuarioActual.getRol());
    }

    public boolean esSupervisor() {
        return usuarioActual != null && "supervisor".equalsIgnoreCase(usuarioActual.getRol());
    }

    public boolean estaLogueado() {
        return usuarioActual != null;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }
}
