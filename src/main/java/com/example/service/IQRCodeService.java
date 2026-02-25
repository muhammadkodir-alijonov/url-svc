package com.example.service;

import com.example.dto.QRCodeColor;
import com.example.dto.QRCodeSize;

/**
 * Interface for QR Code generation operations
 */
public interface IQRCodeService {

    byte[] generateQRCode(String url, int size);

    byte[] generateQRCode(String url);

    byte[] generateQRCode(String url, QRCodeSize size, QRCodeColor foreground, QRCodeColor background);
}
