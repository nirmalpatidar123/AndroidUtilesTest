package com.github.nirmalpatidar123.utils;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// TODO: Auto-generated Javadoc
/**
 * The Class MCrypt.
 * 
 * @author Krishnakant.Dalal
 * @version $Revision: 1.0 $
 */
public class MCrypt {

	/** The hex chars. */
	static char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/** The iv. */
	private String iv = "maalaxmi@23#game";// Dummy iv (CHANGE IT!)

	/** The ivspec. */
	private IvParameterSpec ivspec;

	/** The keyspec. */
	private SecretKeySpec keyspec;

	/** The cipher. */
	private Cipher cipher;

	/** The Secret key. */
	private String SecretKey = "maalaxmi@23#game";// Dummy secretKey (CHANGE
													// IT!)

	/**
	 * Instantiates a new m crypt.
	 */
	public MCrypt() {
		ivspec = new IvParameterSpec(iv.getBytes());

		keyspec = new SecretKeySpec(SecretKey.getBytes(), "AES");

		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Encrypt.
	 * 
	 * @param text
	 *            the text
	 * @return the byte[] * @throws Exception the exception
	 * @throws Exception
	 *             the exception
	 */
	public byte[] encrypt(String text) throws Exception {
		if (text == null || text.length() == 0)
			throw new Exception("Empty string");

		byte[] encrypted = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			encrypted = cipher.doFinal(padString(text).getBytes());
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}

		return encrypted;
	}

	/**
	 * Encrypt.
	 * 
	 * @param text
	 *            the text
	 * @return the byte[] * @throws Exception the exception
	 * @throws Exception
	 *             the exception
	 */
	public String encryptString(String text) throws Exception {
		if (text == null || text.length() == 0)
			throw new Exception("Empty string");

		byte[] encrypted = null;
		String encrypt = null;

		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);

			encrypted = cipher.doFinal(padString(text).getBytes());
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}
		if (encrypted != null) {
			encrypt = bytesToHex(encrypted);
		}
		return encrypt;
	}

	/**
	 * Decrypt.
	 * 
	 * @param code
	 *            the code
	 * @return the byte[] * @throws Exception the exception
	 * @throws Exception
	 *             the exception
	 */
	public byte[] decrypt(String code) throws Exception {
		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		byte[] decrypted = null;

		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(hexToBytes(code));
			// Remove trailing zeroes
			if (decrypted.length > 0) {
				int trim = 0;
				for (int i = decrypted.length - 1; i >= 0; i--)
					if (decrypted[i] == 0)
						trim++;

				if (trim > 0) {
					byte[] newArray = new byte[decrypted.length - trim];
					System.arraycopy(decrypted, 0, newArray, 0,
							decrypted.length - trim);
					decrypted = newArray;
				}
			}
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}

	/**
	 * Decrypt.
	 * 
	 * @param code
	 *            the code
	 * @return the byte[] * @throws Exception the exception
	 * @throws Exception
	 *             the exception
	 */
	public String decryptResult(String code) throws Exception {
		if (code == null || code.length() == 0)
			throw new Exception("Empty string");

		String decryptedString = null;
		byte[] decrypted = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(hexToBytes(code));
			// Remove trailing zeroes
			if (decrypted.length > 0) {
				int trim = 0;
				for (int i = decrypted.length - 1; i >= 0; i--)
					if (decrypted[i] == 0)
						trim++;

				if (trim > 0) {
					byte[] newArray = new byte[decrypted.length - trim];
					System.arraycopy(decrypted, 0, newArray, 0,
							decrypted.length - trim);
					decrypted = newArray;
				}
			}
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		if (decrypted != null) {
			decryptedString = new String(decrypted);
			decryptedString = decryptedString.trim();
		}

		return decryptedString;
	}

	/**
	 * Bytes to hex.
	 * 
	 * @param buf
	 *            the buf
	 * 
	 * @return the string
	 */
	public String bytesToHex(byte[] buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	/**
	 * Hex to bytes.
	 * 
	 * @param str
	 *            the str
	 * 
	 * @return the byte[]
	 */
	public byte[] hexToBytes(String str) {
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

	/**
	 * Pad string.
	 * 
	 * @param source
	 *            the source
	 * 
	 * @return the string
	 */
	private static String padString(String source) {
		char paddingChar = 0;
		int size = 16;
		int x = source.length() % size;
		int padLength = size - x;

		for (int i = 0; i < padLength; i++) {
			source += paddingChar;
		}

		return source;
	}
}
