package com.boit_droid.wallet.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for QR code generation for wallet identification
 */
@Component
public class QRCodeUtil {

    private static final int DEFAULT_QR_CODE_SIZE = 300;
    private static final String IMAGE_FORMAT = "PNG";
    private static final Color FOREGROUND_COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    /**
     * Generate QR code for wallet identification
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @return Base64 encoded QR code image
     * @throws RuntimeException if QR code generation fails
     */
    public String generateWalletQRCode(String walletId, String accountNumber, String accountName) {
        validateInputs(walletId, accountNumber, accountName);
        
        String qrData = encodeWalletData(walletId, accountNumber, accountName);
        return generateQRCodeImage(qrData, DEFAULT_QR_CODE_SIZE);
    }

    /**
     * Generate QR code with custom size
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @param size The size of the QR code (width and height in pixels)
     * @return Base64 encoded QR code image
     * @throws RuntimeException if QR code generation fails
     */
    public String generateWalletQRCode(String walletId, String accountNumber, String accountName, int size) {
        validateInputs(walletId, accountNumber, accountName);
        validateSize(size);
        
        String qrData = encodeWalletData(walletId, accountNumber, accountName);
        return generateQRCodeImage(qrData, size);
    }

    /**
     * Generate QR code for payment request
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @param amount The requested amount
     * @param currency The currency code
     * @param description Optional description
     * @return Base64 encoded QR code image
     * @throws RuntimeException if QR code generation fails
     */
    public String generatePaymentRequestQRCode(String walletId, String accountNumber, String accountName, 
                                             double amount, String currency, String description) {
        validateInputs(walletId, accountNumber, accountName);
        validateAmount(amount);
        validateCurrency(currency);
        
        String qrData = encodePaymentRequestData(walletId, accountNumber, accountName, amount, currency, description);
        return generateQRCodeImage(qrData, DEFAULT_QR_CODE_SIZE);
    }

    /**
     * Encode wallet data for QR code
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @return Encoded wallet data string
     */
    private String encodeWalletData(String walletId, String accountNumber, String accountName) {
        return String.format("WALLET:%s|ACCOUNT:%s|NAME:%s", 
                           walletId, accountNumber, accountName);
    }

    /**
     * Encode payment request data for QR code
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @param amount The requested amount
     * @param currency The currency code
     * @param description Optional description
     * @return Encoded payment request data string
     */
    private String encodePaymentRequestData(String walletId, String accountNumber, String accountName,
                                          double amount, String currency, String description) {
        StringBuilder data = new StringBuilder();
        data.append(String.format("PAYMENT_REQUEST:WALLET:%s|ACCOUNT:%s|NAME:%s|AMOUNT:%.2f|CURRENCY:%s", 
                                 walletId, accountNumber, accountName, amount, currency));
        
        if (description != null && !description.trim().isEmpty()) {
            data.append("|DESC:").append(description.trim());
        }
        
        return data.toString();
    }

    /**
     * Generate QR code image from data
     * @param data The data to encode
     * @param size The size of the QR code
     * @return Base64 encoded QR code image
     * @throws RuntimeException if QR code generation fails
     */
    private String generateQRCodeImage(String data, int size) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
            BufferedImage qrImage = createQRImage(bitMatrix);
            
            return encodeImageToBase64(qrImage);
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode QR code image: " + e.getMessage(), e);
        }
    }

    /**
     * Create BufferedImage from BitMatrix
     * @param bitMatrix The bit matrix from QR code generation
     * @return BufferedImage representation of the QR code
     */
    private BufferedImage createQRImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 
                           FOREGROUND_COLOR.getRGB() : BACKGROUND_COLOR.getRGB());
            }
        }
        
        return image;
    }

    /**
     * Encode BufferedImage to Base64 string
     * @param image The image to encode
     * @return Base64 encoded image string
     * @throws IOException if encoding fails
     */
    private String encodeImageToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, IMAGE_FORMAT, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.getEncoder().encodeToString(imageBytes);
    }

    /**
     * Validate input parameters
     * @param walletId The wallet ID
     * @param accountNumber The account number
     * @param accountName The account name
     * @throws IllegalArgumentException if any input is invalid
     */
    private void validateInputs(String walletId, String accountNumber, String accountName) {
        if (walletId == null || walletId.trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet ID cannot be null or empty");
        }
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be null or empty");
        }
        if (accountName == null || accountName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be null or empty");
        }
    }

    /**
     * Validate QR code size
     * @param size The size to validate
     * @throws IllegalArgumentException if size is invalid
     */
    private void validateSize(int size) {
        if (size < 100 || size > 1000) {
            throw new IllegalArgumentException("QR code size must be between 100 and 1000 pixels");
        }
    }

    /**
     * Validate amount for payment requests
     * @param amount The amount to validate
     * @throws IllegalArgumentException if amount is invalid
     */
    private void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (amount > 1000000) {
            throw new IllegalArgumentException("Amount cannot exceed 1,000,000");
        }
    }

    /**
     * Validate currency code
     * @param currency The currency code to validate
     * @throws IllegalArgumentException if currency is invalid
     */
    private void validateCurrency(String currency) {
        if (currency == null || currency.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency cannot be null or empty");
        }
        if (currency.length() != 3) {
            throw new IllegalArgumentException("Currency code must be 3 characters long");
        }
        if (!currency.matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("Currency code must contain only uppercase letters");
        }
    }

    /**
     * Decode wallet data from QR code content
     * @param qrData The QR code data string
     * @return Map containing wallet information
     * @throws IllegalArgumentException if QR data is invalid
     */
    public Map<String, String> decodeWalletData(String qrData) {
        if (qrData == null || qrData.trim().isEmpty()) {
            throw new IllegalArgumentException("QR data cannot be null or empty");
        }

        Map<String, String> walletInfo = new HashMap<>();
        
        try {
            if (qrData.startsWith("WALLET:")) {
                parseWalletData(qrData, walletInfo);
            } else if (qrData.startsWith("PAYMENT_REQUEST:")) {
                parsePaymentRequestData(qrData, walletInfo);
            } else {
                throw new IllegalArgumentException("Invalid QR code format");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to decode QR data: " + e.getMessage(), e);
        }

        return walletInfo;
    }

    /**
     * Parse wallet data from QR code
     * @param qrData The QR code data
     * @param walletInfo The map to populate with wallet information
     */
    private void parseWalletData(String qrData, Map<String, String> walletInfo) {
        String[] parts = qrData.split("\\|");
        for (String part : parts) {
            if (part.startsWith("WALLET:")) {
                walletInfo.put("walletId", part.substring(7));
            } else if (part.startsWith("ACCOUNT:")) {
                walletInfo.put("accountNumber", part.substring(8));
            } else if (part.startsWith("NAME:")) {
                walletInfo.put("accountName", part.substring(5));
            }
        }
        walletInfo.put("type", "WALLET");
    }

    /**
     * Parse payment request data from QR code
     * @param qrData The QR code data
     * @param walletInfo The map to populate with payment request information
     */
    private void parsePaymentRequestData(String qrData, Map<String, String> walletInfo) {
        String dataWithoutPrefix = qrData.substring(16); // Remove "PAYMENT_REQUEST:"
        String[] parts = dataWithoutPrefix.split("\\|");
        
        for (String part : parts) {
            if (part.startsWith("WALLET:")) {
                walletInfo.put("walletId", part.substring(7));
            } else if (part.startsWith("ACCOUNT:")) {
                walletInfo.put("accountNumber", part.substring(8));
            } else if (part.startsWith("NAME:")) {
                walletInfo.put("accountName", part.substring(5));
            } else if (part.startsWith("AMOUNT:")) {
                walletInfo.put("amount", part.substring(7));
            } else if (part.startsWith("CURRENCY:")) {
                walletInfo.put("currency", part.substring(9));
            } else if (part.startsWith("DESC:")) {
                walletInfo.put("description", part.substring(5));
            }
        }
        walletInfo.put("type", "PAYMENT_REQUEST");
    }
}