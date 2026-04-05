package com.mycompany.activos.service;

import com.mycompany.activos.model.Cliente;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClienteService {

    private static final String HOJA = "Clientes";

    private GoogleSheetsService sheets() throws IOException, GeneralSecurityException {
        return GoogleSheetsService.getInstance();
    }

    public List<Cliente> listarClientes() throws IOException, GeneralSecurityException {
        List<List<Object>> filas = sheets().leerHoja(HOJA);
        List<Cliente> clientes = new ArrayList<>();
        for (List<Object> fila : filas) {
            String nombre = fila.size() > 0 ? String.valueOf(fila.get(0)) : "";
            String rut    = fila.size() > 1 ? String.valueOf(fila.get(1)) : "";
            clientes.add(new Cliente(nombre, rut));
        }
        return clientes;
    }

    public void agregarCliente(Cliente cliente) throws IOException, GeneralSecurityException {
        sheets().agregarFila(HOJA, Arrays.asList(cliente.getCliente(), cliente.getRut()));
    }

    /**
     * @param index índice 0-based en la lista de datos (sin contar encabezado)
     */
    public void actualizarCliente(int index, Cliente cliente) throws IOException, GeneralSecurityException {
        sheets().actualizarFila(HOJA, index, Arrays.asList(cliente.getCliente(), cliente.getRut()));
    }

    /**
     * @param index índice 0-based en la lista de datos (sin contar encabezado)
     */
    public void eliminarCliente(int index) throws IOException, GeneralSecurityException {
        sheets().eliminarFila(HOJA, index);
    }
}
