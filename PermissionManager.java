package com.syssosftintegra.app;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.util.HashMap;

public class PermissionManager {
    private static final String TAG = "PermissionManager";
    private static final String ACTION_USB_PERMISSION = "com.syssosftintegra.app.USB_PERMISSION";

    private final Context context;
    private final Activity activity;

    public PermissionManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public JSObject requestBluetoothPermission() {
        JSObject ret = new JSObject();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            ret.put("success", true);
            ret.put("message", "Ya tiene permisos.");
        } else {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN},
                    1001
            );
            ret.put("success", false);
            ret.put("message", "Permiso Bluetooth solicitado");
        }

        return ret;
    }

    public void requestUsbPermission(int vendorId, int productId, PluginCall call) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = findUsbDevice(usbManager, vendorId, productId);

        if (usbDevice == null) {
            JSObject ret = new JSObject();
            ret.put("success", false);
            ret.put("error", "Dispositivo USB no encontrado: " + vendorId + ":" + productId);
            call.resolve(ret);
            return;
        }

        Log.d(TAG, "Dispositivo encontrado: " + usbDevice.getProductName());

        if (usbManager.hasPermission(usbDevice)) {
            JSObject ret = new JSObject();
            ret.put("success", true);
            ret.put("hasPermission", true);
            ret.put("message", "Permiso USB ya otorgado");
            call.resolve(ret);
        } else {
            requestUsbDevicePermission(usbDevice, call);
        }
    }

    public boolean hasUsbPermission(int vendorId, int productId) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDevice device = findUsbDevice(usbManager, vendorId, productId);
        return device != null && usbManager.hasPermission(device);
    }

    private UsbDevice findUsbDevice(UsbManager usbManager, int vendorId, int productId) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (device.getVendorId() == vendorId && device.getProductId() == productId) {
                return device;
            }
        }
        return null;
    }

    private void requestUsbDevicePermission(UsbDevice device, PluginCall call) {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        Intent intent = new Intent(ACTION_USB_PERMISSION).setPackage(context.getPackageName());

        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_MUTABLE
        );

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        ContextCompat.registerReceiver(
                context,
                createUsbReceiver(call),
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
        );

        usbManager.requestPermission(device, permissionIntent);
    }

    private BroadcastReceiver createUsbReceiver(PluginCall call) {
        return new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {
                    synchronized (this) {
                        boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);

                        JSObject ret = new JSObject();
                        ret.put("success", true);
                        ret.put("hasPermission", granted);
                        ret.put("message", granted ? "Permiso USB otorgado" : "Permiso USB denegado");

                        call.resolve(ret);

                        try {
                            context.unregisterReceiver(this);
                        } catch (Exception e) {
                            Log.w(TAG, "Error al desregistrar receiver: " + e.getMessage());
                        }
                    }
                }
            }
        };
    }
}