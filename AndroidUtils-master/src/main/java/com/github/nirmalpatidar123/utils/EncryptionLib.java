package com.github.nirmalpatidar123.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption class which contains encryption algorithm used in entire project.
 */
public class EncryptionLib {
    /*private final static String KEY = "tQna25tR89d6af1a";
	private final static String IV = "fedcba9876543210";*/

    private String KEY;
    private String IV;

    private EncryptionLib() {
    }

    /**
     * @param KEY
     * @param IV
     */
    public EncryptionLib(String KEY, String IV) {
        this.KEY = KEY;
        this.IV = IV;
    }

//    /**
//     * @param plainText
//     * @return
//     */
//    public String encryptString(String plainText) {
//        try {
//            byte[] b = padString(plainText.trim()).getBytes();
//            byte[] keyBytes = KEY.getBytes();
//
//            // KeyGenerator kgen = KeyGenerator.getInstance("AES");
//            // SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
//            // sr.setSeed(keyBytes);
//            // kgen.init(128, sr); // 192 and 256 bits may not be available
//            // SecretKey skey = kgen.generateKey();
//
//            IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
//            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
//            byte[] encrypted = cipher.doFinal(b);
//
//            return bytesToHex(encrypted);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return plainText;
//        }
//    }
//
//    /**
//     * @param encryptedString
//     * @return
//     */
//    public String decryptString(String encryptedString) {
//        byte[] keyBytes = KEY.getBytes();
//
//        try {
//            IvParameterSpec ivspec = new IvParameterSpec(IV.getBytes());
//            SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
//            cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
//            byte[] decrypted = cipher.doFinal(hexToBytes(encryptedString));
//            String decryptedString = new String(decrypted);
//            return decryptedString.trim();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return encryptedString;
//        }
//    }

    private String bytesToHex(byte[] data) {
        if (data == null) {
            return null;
        }

        int len = data.length;
        String str = "";
        for (int i = 0; i < len; i++) {
            if ((data[i] & 0xFF) < 16)
                str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
            else
                str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
        }
        return str;
    }

    private byte[] hexToBytes(String str) {
        if (str == null) {
            return null;
        } else if (str.length() < 2) {
            return null;
        } else {
            int len = str.length() / 2;
            byte[] buffer = new byte[len];
            for (int i = 0; i < len; i++) {
                buffer[i] = (byte) Integer.parseInt(
                        str.substring(i * 2, i * 2 + 2), 16);
            }
            return buffer;
        }
    }

    private String padString(String source) {
        char paddingChar = ' ';
        int size = 16;
        int x = source.length() % size;
        int padLength = size - x;

        for (int i = 0; i < padLength; i++) {
            source += paddingChar;
        }

        return source;
    }

}