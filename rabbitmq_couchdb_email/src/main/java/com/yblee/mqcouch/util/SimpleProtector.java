package com.yblee.mqcouch.util;

import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;


public class SimpleProtector {

	private static final String ALGORITHM = "AES";
	// length shoud be 16 : set up your own secretKey
	private static final byte[] keyValue = 
			new byte[] { 'T', 'h', 'i', 's', 'I', 's', 'm', 'y', 'S', 'e', 'r', 'e', 't', 'K', 'e', 'y' };

	     public static String encrypt(String valueToEnc) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGORITHM);
	        c.init(Cipher.ENCRYPT_MODE, key);
	        byte[] encValue = c.doFinal(valueToEnc.getBytes());
//	        String encryptedValue = new BASE64Encoder().encode(encValue);
	        String encryptedValue = new String(Base64.encodeBase64(encValue));
	        return encryptedValue;
	    }

	    public static String decrypt(String encryptedValue) throws Exception {
	        Key key = generateKey();
	        Cipher c = Cipher.getInstance(ALGORITHM);
	        c.init(Cipher.DECRYPT_MODE, key);
//	        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedValue);
	        byte[] decordedValue = Base64.decodeBase64(encryptedValue.getBytes());
	        byte[] decValue = c.doFinal(decordedValue);
	        String decryptedValue = new String(decValue);
	        return decryptedValue;
	    }

	    private static Key generateKey() throws Exception {
	        Key key = new SecretKeySpec(keyValue, ALGORITHM);
	        // SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
	        // key = keyFactory.generateSecret(new DESKeySpec(keyValue));
	        return key;
	    }
	
	
}
