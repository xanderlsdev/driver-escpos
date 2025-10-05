package com.syssosftintegra.app;

import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

public class PrinterService {
    private static final String TAG = "PrinterService";

    private final ConnectionManager connectionManager;
    private final EscPosCommands escPosCommands;
    private final ImageProcessor imageProcessor;

    public PrinterService(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        this.escPosCommands = new EscPosCommands();
        this.imageProcessor = new ImageProcessor();
    }

    public void printTicket(PrinterConfig config) throws IOException {
        OutputStream outputStream = connectionManager.getOutputStream(config);

        if (outputStream == null) {
            throw new IOException("No se pudo obtener conexión con la impresora");
        }

        // Inicializar impresora
        outputStream.write(escPosCommands.init());
        outputStream.write(escPosCommands.center());

        // Imprimir imagen o texto
        if (!config.imageUrl.isEmpty()) {
            int printerWidthPx = getPrinterWidthPixels(config.widthMm);
            imageProcessor.printImage(outputStream, config.imageUrl, printerWidthPx, config.speed);
            outputStream.write(escPosCommands.newLine());
        } else {
            outputStream.write(escPosCommands.boldOn());
            outputStream.write((config.message + "\n").getBytes("UTF-8"));
            outputStream.write(escPosCommands.boldOff());
            outputStream.write(escPosCommands.newLine());
        }

        // Cortar papel
        outputStream.write(escPosCommands.cut());
        outputStream.flush();
        outputStream.close();

        Log.d(TAG, "✅ Impresión completada");
    }

    private int getPrinterWidthPixels(int widthMm) {
        return switch (widthMm) {
            case 58 -> 384;
            case 80 -> 576;
            case 48 -> 256;
            default -> 384;
        };
    }
}