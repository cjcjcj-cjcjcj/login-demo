package com.jack.login.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

public class CryptoUtil {

    /** 生成 RSA 密钥对 */
    public static KeyPair generateRsaKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static String rsaPublicKeyToBase64(PublicKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * RSA 解密（传输用）：PKCS1 填充，和前端 JSEncrypt 的默认模式对应
     * 前端 rsaEncrypt(密码) --传输--> 后端 rsaDecrypt 解出明文
     */
    public static String rsaDecrypt(String encryptedBase64, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(encryptedBase64)), StandardCharsets.UTF_8);
    }

    /**
     * RSA 加密（存储/比对用）：NoPadding 是确定性加密，
     * 同样的明文 + 同样的公钥 => 同样的密文，因此可以做 if(xyz==xyz) 的密文比对
     */
    public static String rsaEncrypt(String plain, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }
}
