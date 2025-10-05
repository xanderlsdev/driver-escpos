package com.syssosftintegra.app;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

import androidx.annotation.Nullable;

public class UsbDeviceHelper {

    @Nullable
    static public UsbInterface findPrinterInterface(UsbDevice usbDevice) {
        if (usbDevice == null) {
            return null;
        }
        int interfacesCount = usbDevice.getInterfaceCount();
        for (int i = 0; i < interfacesCount; i++) {
            UsbInterface usbInterface = usbDevice.getInterface(i);
            if (usbInterface.getInterfaceClass() == UsbConstants.USB_CLASS_PRINTER) {
                return usbInterface;
            }
        }
        return null;
    }

    @Nullable
    static public UsbEndpoint findEndpointIn(UsbInterface usbInterface) {
        if (usbInterface != null) {
            int endpointsCount = usbInterface.getEndpointCount();
            for (int i = 0; i < endpointsCount; i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                if (endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.getDirection() == UsbConstants.USB_DIR_OUT) {
                    return endpoint;
                }
            }
        }
        return null;
    }
}

