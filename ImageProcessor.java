package com.syssosftintegra.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageProcessor {

    public void printImage (OutputStream outputStream, String url, int widthPx,boolean speed) throws IOException {
        Bitmap bitmap = getBitmapFromURL(url);

        // Escalar a ancho de impresora
        bitmap = scaleBitmapToPrinterWidth(bitmap, widthPx);

        // Convertir a monocromo
        Bitmap monochrome = convertToMonochrome(bitmap);

        // Mandar a imprimir
        if (speed){
            GSV0Fast(outputStream, monochrome);
        }else{
            GSV0Slow(outputStream, monochrome);
        }
    }

    private Bitmap getBitmapFromURL(String src) throws IOException {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(input);
        input.close();
        return bitmap;
    }

    private Bitmap scaleBitmapToPrinterWidth(Bitmap bitmap, int printerWidthPx) {
        int targetWidth = (printerWidthPx / 8) * 8;
        int targetHeight = (int) (bitmap.getHeight() * ((float) targetWidth / bitmap.getWidth()));
        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    private Bitmap convertToMonochrome(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // Buffer de errores (como en Floyd-Steinberg)
        float[][] error = new float[height][width];

        // 🔧 AJUSTE 1: Reducir gamma para hacer más claro (valor original: 1.1f)
        float gamma = 0.7f; // Prueba valores entre 0.7f - 1.0f

        // 🔧 AJUSTE 2: Cambiar umbral (valor original: 128)
        int threshold = 120; // Prueba valores entre 120-160

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                // Calcular luminancia
                float gray = 0.299f * r + 0.587f * g + 0.114f * b;
                gray = gray / 255.0f;
                gray = (float) Math.pow(gray, gamma) * 255.0f;
                gray = Math.min(255, Math.max(0, gray));

                // Agregar error acumulado
                gray += error[y][x];

                // 🔧 AJUSTE 3: Usar umbral personalizado
                boolean isBlack = gray < threshold;
                int newColor = isBlack ? Color.BLACK : Color.WHITE;
                result.setPixel(x, y, newColor);

                // Calcular error
                float quantError = gray - (isBlack ? 0 : 255);

                // 🔧 AJUSTE 4: Reducir la propagación de error para menos "ruido"
                float errorReduction = 0.8f; // Factor de reducción (0.5f - 1.0f)

                // Propagar error (Floyd-Steinberg con reducción)
                if (x + 1 < width)
                    error[y][x + 1] += quantError * 7 / 16 * errorReduction;
                if (y + 1 < height) {
                    if (x > 0)
                        error[y + 1][x - 1] += quantError * 3 / 16 * errorReduction;
                    error[y + 1][x] += quantError * 5 / 16 * errorReduction;
                    if (x + 1 < width)
                        error[y + 1][x + 1] += quantError * 1 / 16 * errorReduction;
                }
            }
        }

        return result;
    }

    private void GSV0Fast(OutputStream outputStream, Bitmap monochrome) throws IOException {
        int width = monochrome.getWidth();
        int height = monochrome.getHeight();
        int widthBytes = width / 8;

        // Cabecera GS v 0
        byte[] header = new byte[] {
                0x1D, 0x76, 0x30, 0x00,
                (byte)(widthBytes & 0xFF), (byte)((widthBytes >> 8) & 0xFF),
                (byte)(height & 0xFF), (byte)((height >> 8) & 0xFF)
        };

        outputStream.write(header);

        // Buffer intermedio (chunk de 4 KB)
        int kb = 4;
        int size = 1024 * kb;
        ByteArrayOutputStream chunk = new ByteArrayOutputStream(size);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte b = 0;
                for (int bit = 0; bit < 8 && (x + bit) < width; bit++) {
                    int pixel = monochrome.getPixel(x + bit, y);
                    if (Color.red(pixel) == 0) {
                        b |= (byte) (1 << (7 - bit));
                    }
                }
                chunk.write(b);

                // Si el buffer llega a 4 KB → flush
                if (chunk.size() >= size) {
                    outputStream.write(chunk.toByteArray());
                    outputStream.flush();
                    chunk.reset();
                }
            }
        }

        // Lo que quede pendiente
        if (chunk.size() > 0) {
            outputStream.write(chunk.toByteArray());
            outputStream.flush();
        }

        outputStream.write(new byte[]{0x0A});
    }

    private void GSV0Slow(OutputStream outputStream, Bitmap monochrome) throws IOException {
        int width = monochrome.getWidth();
        int height = monochrome.getHeight();
        int widthBytes = width / 8;

        // Comando GSV0: GS v 0 m xL xH yL yH d1...dk
        byte[] header = new byte[] {
                0x1D, 0x76, 0x30, 0x00,  // GS v 0 (modo normal)
                (byte)(widthBytes & 0xFF), (byte)((widthBytes >> 8) & 0xFF),  // xL xH (ancho en bytes)
                (byte)(height & 0xFF), (byte)((height >> 8) & 0xFF)           // yL yH (alto en puntos)
        };

        outputStream.write(header);

        // Convertir bitmap a datos binarios
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte b = 0;
                for (int bit = 0; bit < 8 && x + bit < width; bit++) {
                    int pixel = monochrome.getPixel(x + bit, y);
                    if (Color.red(pixel) == 0) { // Pixel negro
                        b |= (1 << (7 - bit));
                    }
                }
                outputStream.write(b);
            }
        }

        outputStream.write(new byte[]{0x0A}); // Nueva línea al final
    }

}
