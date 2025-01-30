package com.pinnacle.backend.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DecryptionUtil {
    // Use the same key as in EncryptionUtil
    private static final String ENCRYPTION_KEY = "517345ZA28ABAAEF"; // 16-byte key for AES-128

    // Decrypt data
    public static String decrypt(String encryptedData) throws Exception {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new IllegalArgumentException("Encrypted data cannot be null or empty");
        }

        // Generate AES Secret Key
        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");

        // Initialize Cipher in AES/ECB mode
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decode Base64 and decrypt the data
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));

        // Convert to a readable string
        return new String(decryptedBytes);
    }


    // Decrypt APIKey
    public static String decryptAPIKey(String encryptedAPIKey) throws Exception{
        if (encryptedAPIKey == null || encryptedAPIKey.isEmpty()) {
            throw new IllegalArgumentException("Encrypted data cannot be null or empty");
        }

        // Generate AES Secret Key
        SecretKey secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");

        // Initialize Cipher in AES/ECB mode
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // Decode Base64 and decrypt the data
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedAPIKey));

        // Convert to a readable string
        return new String(decryptedBytes);
    }
}
