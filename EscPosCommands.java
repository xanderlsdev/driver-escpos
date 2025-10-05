package com.syssosftintegra.app;

public class EscPosCommands {

    // ========== INICIALIZACIÓN ==========
    public byte[] init() {
        return new byte[]{0x1B, 0x40}; // ESC @ - Reset
    }

    // ========== ALINEACIÓN ==========
    public byte[] left() {
        return new byte[]{0x1B, 0x61, 0x00}; // ESC a 0 - Izquierda
    }

    public byte[] center() {
        return new byte[]{0x1B, 0x61, 0x01}; // ESC a 1 - Centrado
    }

    public byte[] right() {
        return new byte[]{0x1B, 0x61, 0x02}; // ESC a 2 - Derecha
    }

    // ========== TEXTO ==========
    public byte[] boldOn() {
        return new byte[]{0x1B, 0x45, 0x01}; // ESC E 1 - Negrita ON
    }

    public byte[] boldOff() {
        return new byte[]{0x1B, 0x45, 0x00}; // ESC E 0 - Negrita OFF
    }

    public byte[] underlineOn() {
        return new byte[]{0x1B, 0x2D, 0x01}; // ESC - 1 - Subrayado ON (1 punto)
    }

    public byte[] underlineOn2() {
        return new byte[]{0x1B, 0x2D, 0x02}; // ESC - 2 - Subrayado ON (2 puntos)
    }

    public byte[] underlineOff() {
        return new byte[]{0x1B, 0x2D, 0x00}; // ESC - 0 - Subrayado OFF
    }

    public byte[] doubleWidthOn() {
        return new byte[]{0x1B, 0x21, 0x20}; // ESC ! 32 - Doble ancho
    }

    public byte[] doubleHeightOn() {
        return new byte[]{0x1B, 0x21, 0x10}; // ESC ! 16 - Doble alto
    }

    public byte[] doubleStrikeOn() {
        return new byte[]{0x1B, 0x47, 0x01}; // ESC G 1 - Doble impacto
    }

    public byte[] doubleStrikeOff() {
        return new byte[]{0x1B, 0x47, 0x00}; // ESC G 0 - Doble impacto OFF
    }

    public byte[] fontSizeNormal() {
        return new byte[]{0x1B, 0x21, 0x00}; // ESC ! 0 - Tamaño normal
    }

    public byte[] fontSizeLarge() {
        return new byte[]{0x1B, 0x21, 0x30}; // ESC ! 48 - Doble ancho y alto
    }

    public byte[] fontA() {
        return new byte[]{0x1B, 0x4D, 0x00}; // ESC M 0 - Fuente A (12x24)
    }

    public byte[] fontB() {
        return new byte[]{0x1B, 0x4D, 0x01}; // ESC M 1 - Fuente B (9x17)
    }

    public byte[] invertOn() {
        return new byte[]{0x1D, 0x42, 0x01}; // GS B 1 - Blanco sobre negro
    }

    public byte[] invertOff() {
        return new byte[]{0x1D, 0x42, 0x00}; // GS B 0 - Negro sobre blanco
    }

    // ========== TAMAÑO DE CARACTERES ==========
    public byte[] setCharacterSize(int width, int height) {
        // width y height: 0-7 (0 = normal, 1 = 2x, 2 = 3x, etc.)
        int value = ((width & 0x07) << 4) | (height & 0x07);
        return new byte[]{0x1D, 0x21, (byte) value}; // GS ! n
    }

    // ========== LÍNEAS Y ESPACIADO ==========
    public byte[] newLine() {
        return new byte[]{0x0A}; // LF - Salto de línea
    }

    public byte[] carriageReturn() {
        return new byte[]{0x0D}; // CR - Retorno de carro
    }

    public byte[] setLineSpacing(int dots) {
        return new byte[]{0x1B, 0x33, (byte) dots}; // ESC 3 n - Espaciado de línea
    }

    public byte[] resetLineSpacing() {
        return new byte[]{0x1B, 0x32}; // ESC 2 - Espaciado por defecto
    }

    public byte[] horizontalTab() {
        return new byte[]{0x09}; // HT - Tabulación horizontal
    }

    public byte[] setTabPositions(int... positions) {
        byte[] cmd = new byte[positions.length + 2];
        cmd[0] = 0x1B; // ESC
        cmd[1] = 0x44; // D
        for (int i = 0; i < positions.length; i++) {
            cmd[i + 2] = (byte) positions[i];
        }
        return cmd;
    }

    // ========== CORTE DE PAPEL ==========
    public byte[] cut() {
        return new byte[]{0x1D, 0x56, 0x01}; // GS V 1 - Corte parcial
    }

    public byte[] cutFull() {
        return new byte[]{0x1D, 0x56, 0x00}; // GS V 0 - Corte total
    }

    public byte[] cutWithFeed(int lines) {
        return new byte[]{0x1D, 0x56, 0x42, (byte) lines}; // GS V 66 n - Corte con avance
    }

    // ========== CAJÓN DE DINERO ==========
    public byte[] openDrawer1() {
        return new byte[]{0x1B, 0x70, 0x00, 0x19, (byte) 0xFA}; // ESC p 0 - Cajón 1
    }

    public byte[] openDrawer2() {
        return new byte[]{0x1B, 0x70, 0x01, 0x19, (byte) 0xFA}; // ESC p 1 - Cajón 2
    }

    // ========== CÓDIGOS DE BARRAS ==========
    public byte[] barcodeHeight(int height) {
        return new byte[]{0x1D, 0x68, (byte) height}; // GS h n - Altura del código
    }

    public byte[] barcodeWidth(int width) {
        return new byte[]{0x1D, 0x77, (byte) width}; // GS w n - Ancho (2-6)
    }

    public byte[] barcodeTextPosition(int position) {
        // 0: No texto, 1: Arriba, 2: Abajo, 3: Arriba y abajo
        return new byte[]{0x1D, 0x48, (byte) position}; // GS H n
    }

    public byte[] printBarcodeEAN13(String data) {
        byte[] dataBytes = data.getBytes();
        byte[] cmd = new byte[4 + dataBytes.length];
        cmd[0] = 0x1D; // GS
        cmd[1] = 0x6B; // k
        cmd[2] = 0x02; // EAN13
        cmd[3] = (byte) dataBytes.length;
        System.arraycopy(dataBytes, 0, cmd, 4, dataBytes.length);
        return cmd;
    }

    public byte[] printBarcodeCode128(String data) {
        byte[] dataBytes = data.getBytes();
        byte[] cmd = new byte[4 + dataBytes.length];
        cmd[0] = 0x1D; // GS
        cmd[1] = 0x6B; // k
        cmd[2] = 0x49; // CODE128
        cmd[3] = (byte) dataBytes.length;
        System.arraycopy(dataBytes, 0, cmd, 4, dataBytes.length);
        return cmd;
    }

    // ========== QR CODE ==========
    public byte[] qrCodeSize(int size) {
        // size: 1-16 (1 = más pequeño)
        return new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x43, (byte) size}; // GS ( k - Tamaño
    }

    public byte[] qrCodeErrorCorrection(int level) {
        // level: 48(L), 49(M), 50(Q), 51(H)
        return new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, (byte) level}; // GS ( k - Error
    }

    public byte[] qrCodeStore(String data) {
        byte[] dataBytes = data.getBytes();
        int length = dataBytes.length + 3;
        byte[] cmd = new byte[8 + dataBytes.length];
        cmd[0] = 0x1D; // GS
        cmd[1] = 0x28; // (
        cmd[2] = 0x6B; // k
        cmd[3] = (byte) (length & 0xFF);
        cmd[4] = (byte) ((length >> 8) & 0xFF);
        cmd[5] = 0x31; // 1
        cmd[6] = 0x50; // P
        cmd[7] = 0x30; // 0
        System.arraycopy(dataBytes, 0, cmd, 8, dataBytes.length);
        return cmd;
    }

    public byte[] qrCodePrint() {
        return new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30}; // GS ( k - Imprimir
    }

    // ========== PAPEL ==========
    public byte[] feedLines(int lines) {
        return new byte[]{0x1B, 0x64, (byte) lines}; // ESC d n - Avanzar n líneas
    }

    public byte[] feedDots(int dots) {
        return new byte[]{0x1B, 0x4A, (byte) dots}; // ESC J n - Avanzar n puntos
    }

    // ========== PÁGINA ==========
    public byte[] selectPageMode() {
        return new byte[]{0x1B, 0x4C}; // ESC L - Modo página
    }

    public byte[] selectStandardMode() {
        return new byte[]{0x1B, 0x53}; // ESC S - Modo estándar
    }

    // ========== BUZZER ==========
    public byte[] beep(int times, int duration) {
        // times: 1-9, duration: 1-9 (en 100ms)
        return new byte[]{0x1B, 0x42, (byte) times, (byte) duration}; // ESC B n t
    }

    // ========== UTILIDADES ==========
    public byte[] setCharacterCodeTable(int table) {
        // table: 0-255 (depende de la impresora)
        return new byte[]{0x1B, 0x74, (byte) table}; // ESC t n
    }

    public byte[] setInternationalCharset(int charset) {
        // 0: USA, 1: Francia, 2: Alemania, etc.
        return new byte[]{0x1B, 0x52, (byte) charset}; // ESC R n
    }

    // ========== COMANDOS COMBINADOS ==========
    public byte[] printLineWithSeparator(String text, char separator) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            line.append(separator);
        }
        return line.toString().getBytes();
    }

    public byte[] printDashedLine(int length) {
        return new String(new char[length]).replace('\0', '-').getBytes();
    }

    public byte[] printDoubleLine(int length) {
        return new String(new char[length]).replace('\0', '=').getBytes();
    }
}