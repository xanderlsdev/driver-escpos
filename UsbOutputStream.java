package com.syssosftintegra.app;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

public class UsbOutputStream extends OutputStream {
    private static final String TAG = "UsbOutputStream";

    private final UsbDeviceConnection connection;
    private final UsbEndpoint endpoint;
    private final UsbInterface usbInterface;

    public UsbOutputStream(UsbDeviceConnection connection, UsbEndpoint endpoint, UsbInterface usbInterface) {
        this.connection = connection;
        this.endpoint = endpoint;
        this.usbInterface = usbInterface;
    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte) b});
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        int offset = 0;
        while (offset < bytes.length) {
            int chunk = Math.min(16384, bytes.length - offset); // 16KB por bloque
            int result = connection.bulkTransfer(endpoint,
                    Arrays.copyOfRange(bytes, offset, offset + chunk),
                    chunk,
                    5000);
            if (result < 0) {
                throw new IOException("Error en transferencia USB: " + result);
            }
            offset += chunk;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            connection.releaseInterface(usbInterface);
            connection.close();
        } catch (Exception e) {
            Log.w(TAG, "Error cerrando conexión USB: " + e.getMessage());
        }
        super.close();
    }
}