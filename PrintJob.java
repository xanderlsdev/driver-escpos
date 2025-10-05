package com.syssosftintegra.app;

import com.getcapacitor.PluginCall;

public class PrintJob {
    public final PrinterConfig config;
    public final PluginCall call;

    public PrintJob(PrinterConfig config, PluginCall call) {
        this.config = config;
        this.call = call;
    }
}