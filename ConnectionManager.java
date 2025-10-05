package com.syssosftintegra.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.UUID;

public class ConnectionManager {
    private static final String TAG = "ConnectionManager";

    private final Context context;
    private BluetoothSocket currentBluetoothSocket = null;
    private UsbDeviceConnection currentUsbConnection = null;
    private UsbInterface currentUsbInterface = null;
    private Socket currentNetworkSocket = null;
    private OutputStream currentOutputStream = null;

    public ConnectionManager(Context context) {
        this.context = context;
    }

    public OutputStream getOutputStream(PrinterConfig config) throws IOException {
        closeAll();

        switch (config.type) {
            case USB:
                currentOutputStream = getUsbOutputStream(config.vendorId, config.productId);
                break;
            case BLUETOOTH:
                currentOutputStream = getBluetoothOutputStream(config.address);
                break;
            case NETWORK:
                currentOutputStream = getNetworkOutputStream(config.address, config.port);
                break;
            default:
                throw new IOException("Tipo de impresora no soportado: " + config.type);
        }

        return currentOutputStream;
    }

    private OutputStream getBluetoothOutputStream(String macAddress) throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            throw new IOException("Bluetooth no disponible o apagado");
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            throw new IOException("No tiene permiso BLUETOOTH_CONNECT");
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        currentBluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);

        bluetoothAdapter.cancelDiscovery();
        currentBluetoothSocket.connect();

        return currentBluetoothSocket.getOutputStream();
    }

    private OutputStream getUsbOutputStream(int vendorId, int productId) throws IOException {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbDevice usbDevice = findUsbDevice(usbManager, vendorId, productId);

        if (usbDevice == null) {
            throw new IOException("Dispositivo USB no encontrado: " + vendorId + ":" + productId);
        }

        if (!usbManager.hasPermission(usbDevice)) {
            throw new IOException("Sin permisos USB para el dispositivo: " + vendorId + ":" + productId);
        }

        currentUsbInterface = UsbDeviceHelper.findPrinterInterface(usbDevice);
        if (currentUsbInterface == null) {
            throw new IOException("Interface de impresora no encontrada en el dispositivo USB");
        }

        currentUsbConnection = usbManager.openDevice(usbDevice);
        if (currentUsbConnection == null) {
            throw new IOException("No se pudo abrir conexión USB");
        }

        if (!currentUsbConnection.claimInterface(currentUsbInterface, true)) {
            currentUsbConnection.close();
            throw new IOException("No se pudo reclamar la interfaz USB");
        }

        UsbEndpoint endpoint = UsbDeviceHelper.findEndpointIn(currentUsbInterface);
        if (endpoint == null) {
            currentUsbConnection.releaseInterface(currentUsbInterface);
            currentUsbConnection.close();
            throw new IOException("Endpoint USB no encontrado");
        }

        return new UsbOutputStream(currentUsbConnection, endpoint, currentUsbInterface);
    }

    private OutputStream getNetworkOutputStream(String ipAddress, int port) throws IOException {
        currentNetworkSocket = new Socket(ipAddress, port);
        currentNetworkSocket.setSoTimeout(5000);
        return currentNetworkSocket.getOutputStream();
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

    public void closeAll() {
        try {
            if (currentOutputStream != null) {
                currentOutputStream.close();
                currentOutputStream = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando OutputStream: " + e.getMessage());
        }

        try {
            if (currentUsbConnection != null && currentUsbInterface != null) {
                currentUsbConnection.releaseInterface(currentUsbInterface);
                currentUsbInterface = null;
            }
            if (currentUsbConnection != null) {
                currentUsbConnection.close();
                currentUsbConnection = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando USB: " + e.getMessage());
        }

        try {
            if (currentBluetoothSocket != null) {
                currentBluetoothSocket.close();
                currentBluetoothSocket = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando Bluetooth: " + e.getMessage());
        }

        try {
            if (currentNetworkSocket != null) {
                currentNetworkSocket.close();
                currentNetworkSocket = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando Socket de red: " + e.getMessage());
        }
    }
}