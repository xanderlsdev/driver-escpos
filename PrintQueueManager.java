package com.syssosftintegra.app;

import android.util.Log;
import com.getcapacitor.JSObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PrintQueueManager {
    private static final String TAG = "PrintQueueManager";
    private final PrinterPlugin plugin;
    private final BlockingQueue<PrintJob> queue = new LinkedBlockingQueue<>();
    private final PrinterService printerService;
    private final ConnectionManager connectionManager;
    private volatile boolean running = true;

    public PrintQueueManager(PrinterPlugin plugin,ConnectionManager connectionManager, PrinterService printerService) {
        this.plugin = plugin;
        this.connectionManager = connectionManager;
        this.printerService = printerService;

        Thread workerThread = new Thread(this::processQueue, "PrintQueueWorker");
        workerThread.setDaemon(true);
        workerThread.start();
    }

    public void addJob(PrintJob job) {
        queue.offer(job);
        Log.d(TAG, "📝 Trabajo agregado a la cola. Total pendientes: " + queue.size());
    }
    public void stopWorker() {
        running = false;
    }

    private void processQueue() {
        while (running) {
            try {
                PrintJob job = queue.take(); // Espera un nuevo trabajo
                Log.d(TAG, "▶️ Procesando impresión...");

                // Notificar inicio
                JSObject start = new JSObject();
                start.put("id", job.config.id);
                start.put("status", "printing");
                start.put("message", "Imprimiendo...");
                plugin.sendJobUpdate(start);

                try {
                    // Se procesa la impresión
                    printerService.printTicket(job.config);

                    // Notificar éxito
                    JSObject success = new JSObject();
                    success.put("id", job.config.id);
                    success.put("status", "success");
                    success.put("message", "Impresión completada con éxito");
                    plugin.sendJobUpdate(success);
                } catch (Exception e) {
                    Log.e(TAG, "❌ Error al imprimir", e);

                    // Notificar error
                    JSObject error = new JSObject();
                    error.put("id", job.config.id);
                    error.put("status", "error");
                    error.put("message", e.getMessage());
                    plugin.sendJobUpdate(error);
                } finally {
                    connectionManager.closeAll();
                }

            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Worker interrumpido", e);

                JSObject interrupt = new JSObject();
                interrupt.put("status", "error");
                interrupt.put("message", "Worker interrumpido");
                plugin.sendJobUpdate(interrupt);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
