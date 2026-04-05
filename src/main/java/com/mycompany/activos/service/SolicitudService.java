package com.mycompany.activos.service;

import com.mycompany.activos.model.Solicitud;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SolicitudService {

    private static final String HOJA = "Solicitudes";

    private GoogleSheetsService sheets() throws IOException, GeneralSecurityException {
        return GoogleSheetsService.getInstance();
    }

    private String col(List<Object> fila, int idx) {
        return (fila.size() > idx) ? String.valueOf(fila.get(idx)).trim() : "";
    }

    public List<Solicitud> listarSolicitudes() throws IOException, GeneralSecurityException {
        List<List<Object>> filas = sheets().leerHoja(HOJA);
        List<Solicitud> lista = new ArrayList<>();
        for (List<Object> fila : filas) {
            lista.add(new Solicitud(
                col(fila, 0), col(fila, 1), col(fila, 2), col(fila, 3),
                col(fila, 4), col(fila, 5), col(fila, 6), col(fila, 7),
                col(fila, 8), col(fila, 9), col(fila, 10), col(fila, 11)
            ));
        }
        return lista;
    }

    public void agregarSolicitud(Solicitud s) throws IOException, GeneralSecurityException {
        sheets().agregarFila(HOJA, Arrays.asList(
            s.getFechaSolicitud(), s.getClienteSolicitante(), s.getCargo(), s.getSolicitudes(),
            s.getReclutados(), s.getValidacion(), s.getCitacion(), s.getAsistencia(),
            s.getFiltroAprovados(), s.getSeleccionados(), s.getEstado(), s.getSupervisor()
        ));
    }

    /** index 0-based sobre la lista de datos (sin encabezado) */
    public void actualizarSolicitud(int index, Solicitud s) throws IOException, GeneralSecurityException {
        sheets().actualizarFila(HOJA, index, Arrays.asList(
            s.getFechaSolicitud(), s.getClienteSolicitante(), s.getCargo(), s.getSolicitudes(),
            s.getReclutados(), s.getValidacion(), s.getCitacion(), s.getAsistencia(),
            s.getFiltroAprovados(), s.getSeleccionados(), s.getEstado(), s.getSupervisor()
        ));
    }

    public void eliminarSolicitud(int index) throws IOException, GeneralSecurityException {
        sheets().eliminarFila(HOJA, index);
    }
}
