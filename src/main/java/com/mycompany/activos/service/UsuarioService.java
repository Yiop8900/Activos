package com.mycompany.activos.service;

import com.mycompany.activos.model.Usuario;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsuarioService {

    private static final String HOJA = "Usuarios_Roles";

    private GoogleSheetsService sheets() throws IOException, GeneralSecurityException {
        return GoogleSheetsService.getInstance();
    }

    /**
     * Valida credenciales contra la hoja Usuarios.
     * @return el Usuario si las credenciales son correctas, null en caso contrario.
     */
    public Usuario login(String usuario, String contrasena) throws IOException, GeneralSecurityException {
        List<List<Object>> filas = sheets().leerHoja(HOJA);
        for (List<Object> fila : filas) {
            String u = fila.size() > 0 ? String.valueOf(fila.get(0)).trim() : "";
            String p = fila.size() > 1 ? String.valueOf(fila.get(1)).trim() : "";
            String r = fila.size() > 2 ? String.valueOf(fila.get(2)).trim() : "";
            if (u.equalsIgnoreCase(usuario.trim()) && p.equals(contrasena)) {
                return new Usuario(u, p, r);
            }
        }
        return null;
    }

    public List<Usuario> listarUsuarios() throws IOException, GeneralSecurityException {
        List<List<Object>> filas = sheets().leerHoja(HOJA);
        List<Usuario> usuarios = new ArrayList<>();
        for (List<Object> fila : filas) {
            String u = fila.size() > 0 ? String.valueOf(fila.get(0)) : "";
            String p = fila.size() > 1 ? String.valueOf(fila.get(1)) : "";
            String r = fila.size() > 2 ? String.valueOf(fila.get(2)) : "";
            usuarios.add(new Usuario(u, p, r));
        }
        return usuarios;
    }

    public void agregarUsuario(Usuario usuario) throws IOException, GeneralSecurityException {
        sheets().agregarFila(HOJA, Arrays.asList(
            usuario.getUsuario(), usuario.getContrasena(), usuario.getRol()
        ));
    }

    public void actualizarUsuario(int index, Usuario usuario) throws IOException, GeneralSecurityException {
        sheets().actualizarFila(HOJA, index, Arrays.asList(
            usuario.getUsuario(), usuario.getContrasena(), usuario.getRol()
        ));
    }

    public void eliminarUsuario(int index) throws IOException, GeneralSecurityException {
        sheets().eliminarFila(HOJA, index);
    }

    /** Retorna lista de nombres de usuario cuyo rol sea "supervisor" (comparacion case-insensitive). */
    public List<String> listarNombresSupervisores() throws IOException, GeneralSecurityException {
        List<List<Object>> filas = sheets().leerHoja(HOJA);
        List<String> supervisores = new ArrayList<>();
        for (List<Object> fila : filas) {
            String r = fila.size() > 2 ? String.valueOf(fila.get(2)).trim() : "";
            if ("supervisor".equalsIgnoreCase(r)) {
                String u = fila.size() > 0 ? String.valueOf(fila.get(0)).trim() : "";
                if (!u.isEmpty()) supervisores.add(u);
            }
        }
        return supervisores;
    }
}
