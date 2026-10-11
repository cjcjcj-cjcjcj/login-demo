package com.jack.login.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class CryptoUtil {
    public static String aesEncrypt(String plain,String key) throws Exception{
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
    public static String aesDecrypt(String encrypt,String key) throws Exception{
        Cipher cipher =Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE,new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8),"AES"));
        return new String(cipher.doFinal(Base64.getDecoder().decode(encrypt)),StandardCharsets.UTF_8);
    }

    public static KeyPair generateRseKeyPair() throws Exception{
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }
    public static String rsaPublicKeyToBase64(PublicKey Key){
        return Base64.getEncoder().encodeToString(Key.getEncoded());
    }
    public static String rseDecrypt(String encryptedBase64, PrivateKey privateKey)throws Exception{
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedBase64)),StandardCharsets.UTF_8);
    }
}
