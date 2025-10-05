package com.syssosftintegra.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.core.app.ActivityCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.util.HashMap;
import java.util.Set;

public class PrinterDiscovery {
    private final Context context;

    public PrinterDiscovery(Context context) {
        this.context = context;
    }

    public JSObject listBluetoothPrinters() {
        JSObject result = new JSObject();
        JSArray devicesArray = new JSArray();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled() &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            for (BluetoothDevice device : pairedDevices) {
                int majDeviceCl = device.getBluetoothClass().getMajorDeviceClass();
                int deviceCl = device.getBluetoothClass().getDeviceClass();

                if (majDeviceCl == BluetoothClass.Device.Major.IMAGING &&
                        (deviceCl == 1664 || deviceCl == BluetoothClass.Device.Major.IMAGING)) {
                    JSObject deviceInfo = new JSObject();
                    deviceInfo.put("name", device.getName());
                    deviceInfo.put("address", device.getAddress());
                    deviceInfo.put("type", "BLUETOOTH");
                    devicesArray.put(deviceInfo);
                }
            }
        }

        result.put("devices", devicesArray);
        return result;
    }

    public JSObject listUsbPrinters() {
        JSObject result = new JSObject();
        JSArray devicesArray = new JSArray();

        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        for (UsbDevice device : deviceList.values()) {
            int usbClass = device.getDeviceClass();

            if ((usbClass == UsbConstants.USB_CLASS_PER_INTERFACE || usbClass == UsbConstants.USB_CLASS_MISC)
                    && UsbDeviceHelper.findPrinterInterface(device) != null) {
                usbClass = UsbConstants.USB_CLASS_PRINTER;
            }

            if (usbClass == UsbConstants.USB_CLASS_PRINTER) {
                JSObject deviceInfo = new JSObject();
                deviceInfo.put("name", device.getDeviceName());
                deviceInfo.put("vendorId", device.getVendorId());
                deviceInfo.put("productId", device.getProductId());
                deviceInfo.put("productName", device.getProductName());
                deviceInfo.put("manufacturerName", device.getManufacturerName());
                deviceInfo.put("type", "USB");
                devicesArray.put(deviceInfo);
            }
        }

        result.put("devices", devicesArray);
        return result;
    }
}