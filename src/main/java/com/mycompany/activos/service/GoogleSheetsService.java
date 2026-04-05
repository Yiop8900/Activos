package com.mycompany.activos.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GoogleSheetsService {

    private static final String SPREADSHEET_ID = "1uf7sLOV5KU31_KUqTl58w43lU6UotHQ2v5OPE0-Y9sw";
    private static final String APPLICATION_NAME = "AppJava";

    private static GoogleSheetsService instancia;
    private Sheets sheetsService;

    private GoogleSheetsService() throws IOException, GeneralSecurityException {
        InputStream credentialsStream = getClass().getResourceAsStream("/credentials.json");
        if (credentialsStream == null) {
            throw new IOException(
                "No se encontró credentials.json en src/main/resources/.\n" +
                "Consulta las instrucciones de configuración de Google Cloud."
            );
        }

        GoogleCredentials credenciales = GoogleCredentials
            .fromStream(credentialsStream)
            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        sheetsService = new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            new HttpCredentialsAdapter(credenciales)
        ).setApplicationName(APPLICATION_NAME).build();
    }

    public static GoogleSheetsService getInstance() throws IOException, GeneralSecurityException {
        if (instancia == null) {
            instancia = new GoogleSheetsService();
        }
        return instancia;
    }

    /**
     * Lee todas las filas de datos de una hoja (omite la fila de encabezados).
     * Retorna lista vacía si no hay datos.
     */
    public List<List<Object>> leerHoja(String hoja) throws IOException {
        String rango = hoja + "!A2:Z";
        ValueRange respuesta = sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID, rango)
            .execute();
        List<List<Object>> valores = respuesta.getValues();
        return (valores != null) ? valores : Collections.emptyList();
    }

    /**
     * Agrega una fila al final de la hoja.
     */
    public void agregarFila(String hoja, List<Object> fila) throws IOException {
        String rango = hoja + "!A1";
        ValueRange cuerpo = new ValueRange().setValues(Collections.singletonList(fila));
        sheetsService.spreadsheets().values()
            .append(SPREADSHEET_ID, rango, cuerpo)
            .setValueInputOption("USER_ENTERED")
            .setInsertDataOption("INSERT_ROWS")
            .execute();
    }

    /**
     * Actualiza una fila en la hoja.
     * @param filaIndex índice 0-based en el array de datos (excluye encabezado).
     *                  La fila real en el sheet = filaIndex + 2 (encabezado en fila 1).
     */
    public void actualizarFila(String hoja, int filaIndex, List<Object> valores) throws IOException {
        int filaSheet = filaIndex + 2; // encabezado es fila 1
        String rango = hoja + "!A" + filaSheet + ":Z" + filaSheet;
        ValueRange cuerpo = new ValueRange().setValues(Collections.singletonList(valores));
        sheetsService.spreadsheets().values()
            .update(SPREADSHEET_ID, rango, cuerpo)
            .setValueInputOption("USER_ENTERED")
            .execute();
    }

    /**
     * Elimina una fila de la hoja usando su índice 0-based en el array de datos.
     * La fila en el sheet = filaIndex + 1 (0-based en sheet, encabezado ocupa índice 0).
     */
    public void eliminarFila(String hoja, int filaIndex) throws IOException, GeneralSecurityException {
        // Primero necesitamos el sheetId numérico de la hoja por nombre
        Integer sheetId = obtenerSheetId(hoja);
        if (sheetId == null) {
            throw new IOException("No se encontró la hoja: " + hoja);
        }

        int filaSheetIndex0 = filaIndex + 1; // +1 porque el encabezado ocupa índice 0 (0-based)

        DeleteDimensionRequest deleteRequest = new DeleteDimensionRequest()
            .setRange(new DimensionRange()
                .setSheetId(sheetId)
                .setDimension("ROWS")
                .setStartIndex(filaSheetIndex0)
                .setEndIndex(filaSheetIndex0 + 1)
            );

        Request request = new Request().setDeleteDimension(deleteRequest);
        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
            .setRequests(Collections.singletonList(request));

        sheetsService.spreadsheets().batchUpdate(SPREADSHEET_ID, batchRequest).execute();
    }

    private Integer obtenerSheetId(String nombreHoja) throws IOException {
        var spreadsheet = sheetsService.spreadsheets().get(SPREADSHEET_ID).execute();
        for (var sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equalsIgnoreCase(nombreHoja)) {
                return sheet.getProperties().getSheetId();
            }
        }
        return null;
    }
}
