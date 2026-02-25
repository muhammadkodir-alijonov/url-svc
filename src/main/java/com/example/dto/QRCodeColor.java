package com.example.dto;

/**
 * Predefined colors for QR codes
 */
public enum QRCodeColor {
    BLACK(0xFF000000),
    WHITE(0xFFFFFFFF),
    RED(0xFFE53935),
    BLUE(0xFF1E88E5),
    GREEN(0xFF43A047),
    PURPLE(0xFF8E24AA),
    ORANGE(0xFFFB8C00),
    PINK(0xFFD81B60),
    CYAN(0xFF00ACC1),
    INDIGO(0xFF3949AB);

    private final int argb;

    QRCodeColor(int argb) {
        this.argb = argb;
    }

    public int getArgb() {
        return argb;
    }
}
