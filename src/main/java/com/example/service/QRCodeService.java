package com.example.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@ApplicationScoped
public class QRCodeService {

    private static final Logger LOG = Logger.getLogger(QRCodeService.class);

    private static final int DEFAULT_SIZE = 256;
    private static final int MIN_SIZE = 64;
    private static final int MAX_SIZE = 1024;

    /**
     * Generate QR code as PNG byte array
     *
     * @param url URL to encode
     * @param size QR code size (width and height)
     * @return PNG image as byte array
     */
    public byte[] generateQRCode(String url, int size) {
        // Validate size
        int qrSize = size;
        if (qrSize < MIN_SIZE || qrSize > MAX_SIZE) {
            LOG.warnf("Invalid QR size %d, using default %d", size, DEFAULT_SIZE);
            qrSize = DEFAULT_SIZE;
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    url,
                    BarcodeFormat.QR_CODE,
                    qrSize,
                    qrSize
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            LOG.debugf("Generated QR code for URL: %s (size: %d)", url, qrSize);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            LOG.errorf("Failed to generate QR code for URL %s: %s", url, e.getMessage());
            throw new RuntimeException("QR code generation failed", e);
        }
    }

    /**
     * Generate QR code with default size
     */
    public byte[] generateQRCode(String url) {
        return generateQRCode(url, DEFAULT_SIZE);
    }
}