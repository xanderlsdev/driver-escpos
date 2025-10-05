package com.syssosftintegra.app;

import com.getcapacitor.PluginCall;

public class PrinterConfig {
    //
    public  long id;
    //
    public PrinterType type;
    //
    public String address;
    //
    public int vendorId;
    //
    public int productId;
    //
    public int port = 9100;
    //
    public int widthMm = 58;
    //
    public String message = "";
    //
    public String imageUrl = "";
    //
    public boolean speed;

    public static PrinterConfig fromCall(PluginCall call) {
        PrinterConfig config = new PrinterConfig();
        config.id = call.getLong("id", 0L);
        config.type = PrinterType.valueOf(call.getString("type", "USB").toUpperCase());
        config.address = call.getString("address", "");
        config.vendorId = call.getInt("vendorId", 0);
        config.productId = call.getInt("productId", 0);
        config.port = call.getInt("port", 9100);
        config.widthMm = call.getInt("widthMm", 58);
        config.message = call.getString("message", "Ticket test ESC/POS 🚀");
        config.imageUrl = call.getString("imageUrl", "");
        config.speed = call.getBoolean("speed", true);
        return config;
    }

}
