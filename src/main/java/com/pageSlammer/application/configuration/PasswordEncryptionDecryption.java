package com.pageSlammer.application.configuration;

import java.security.Key;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class PasswordEncryptionDecryption{
	public String encrypt(String password) throws Exception {
	Key secretKey = new SecretKeySpec("N<c]3}:),*wDzB)fV\\_Kb2S-eb)K8B#S".getBytes(), "AES");
    Cipher c = Cipher.getInstance("AES");
    c.init(Cipher.ENCRYPT_MODE, secretKey);
    byte[] encValue = c.doFinal(password.getBytes());
    String encryptedValue = Base64.getEncoder().encodeToString(encValue);
    return encryptedValue;
	}
	public String decrypt(String encryptedValue) throws Exception {
		Key secretKey = new SecretKeySpec("N<c]3}:),*wDzB)fV\\_Kb2S-eb)K8B#S".getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decordedValue = Base64.getDecoder().decode(encryptedValue);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
}
