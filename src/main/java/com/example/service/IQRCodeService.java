package com.example.service;

/**
 * Interface for QR Code generation operations
 */
public interface IQRCodeService {

    /**
     * Generate QR code for a given URL
     *
     * @param url the URL to encode in QR code
     * @param size the size of QR code (width and height in pixels)
     * @return byte array containing PNG image of QR code
     */
    byte[] generateQRCode(String url, int size);

    /**
     * Generate QR code with default size (300x300)
     *
     * @param url the URL to encode in QR code
     * @return byte array containing PNG image of QR code
     */
    byte[] generateQRCode(String url);
}
