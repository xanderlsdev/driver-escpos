package com.syssosftintegra.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "PrinterPlugin")
public class PrinterPlugin extends Plugin {

    private static final String TAG = "PrinterPlugin";
    private PermissionManager permissionManager;
    private PrinterDiscovery printerDiscovery;
    private PrintQueueManager printQueueManager;

    @Override
    public void load() {
        super.load();
        Context context = getContext();

        ConnectionManager connectionManager = new ConnectionManager(context);
        permissionManager = new PermissionManager(context, getActivity());
        printerDiscovery = new PrinterDiscovery(context);
        PrinterService printerService = new PrinterService(connectionManager);
        printQueueManager = new PrintQueueManager(this,connectionManager, printerService);
    }

    @PluginMethod
    public void requestBluetoothPermission(PluginCall call) {
        JSObject result = permissionManager.requestBluetoothPermission();
        call.resolve(result);
    }

    @PluginMethod
    public void requestUsbPermission(PluginCall call) {
        try {
            int vendorId = call.getInt("vendorId", 0);
            int productId = call.getInt("productId", 0);

            if (vendorId == 0 || productId == 0) {
                JSObject ret = new JSObject();
                ret.put("success", false);
                ret.put("error", "vendorId y productId son requeridos");
                call.resolve(ret);
                return;
            }

            permissionManager.requestUsbPermission(vendorId, productId, call);

        } catch (Exception ex) {
            JSObject ret = new JSObject();
            ret.put("success", false);
            ret.put("error", ex.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void printTicket(PluginCall call) {
        PrinterConfig config = PrinterConfig.fromCall(call);

        try {
            Log.d(TAG, "🖨️ Imprimiendo: "+ "id: "+ config.id +" - " + config.type + " - Vendor: " + config.vendorId + " Product: " + config.productId + " - Address: "+ config.address);

            // Verificar permisos para USB
            if (config.type == PrinterType.USB) {
                if (!permissionManager.hasUsbPermission(config.vendorId, config.productId)) {
                    JSObject ret = new JSObject();
                    ret.put("id", config.id);
                    ret.put("success", false);
                    ret.put("message", "Sin permisos USB. Ejecuta requestUsbPermission() primero");
                    ret.put("needsPermission", true);
                    call.resolve(ret);
                    return;
                }
            }

            // Verificar permisos para Bluetooth
            if (config.type == PrinterType.BLUETOOTH) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
                    JSObject ret = new JSObject();
                    ret.put("id", config.id);
                    ret.put("success", false);
                    ret.put("message", "Sin permisos Bluetooth. Ejecuta requestBluetoothPermission() primero");
                    ret.put("needsPermission", true);
                    call.resolve(ret);
                    return;
                }
            }

            // Agregar trabajo a la cola
            printQueueManager.addJob(new PrintJob(config, call));

            // Respuesta inmediata indicando que fue encolado
            JSObject ret = new JSObject();
            ret.put("id", config.id);
            ret.put("success", true);
            ret.put("message", "Impresión registrada en la cola.");
            call.resolve(ret);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error al imprimir", e);
            JSObject ret = new JSObject();
            ret.put("id", config.id);
            ret.put("success", false);
            ret.put("message", e.getMessage());
            call.resolve(ret);
        }
    }

    @PluginMethod
    public void listPrinters(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("bluetooth", printerDiscovery.listBluetoothPrinters());
        ret.put("usb", printerDiscovery.listUsbPrinters());
        call.resolve(ret);
    }

    public void sendJobUpdate(JSObject data) {
        notifyListeners("onPrintJobUpdate", data);
    }

}
