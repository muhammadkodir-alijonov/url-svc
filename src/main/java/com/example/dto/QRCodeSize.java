package com.example.dto;

/**
 * Standard QR code sizes
 */
public enum QRCodeSize {
    SMALL(128),
    MEDIUM(256),
    LARGE(512);

    private final int pixels;

    QRCodeSize(int pixels) {
        this.pixels = pixels;
    }

    public int getPixels() {
        return pixels;
    }
}
