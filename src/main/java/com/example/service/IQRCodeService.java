package com.example.service;

/**
 * Interface for QR Code generation operations
 */
public interface IQRCodeService {

    byte[] generateQRCode(String url, int size);

    byte[] generateQRCode(String url);
}
