package tz.go.moh.him.thscp.mediator.muse.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;

public class RSAUtils {
    public static final String ALGORITHM = "SHA256withRSA";
    public static final String PKCS_12 = "PKCS12";
    private static final Logger log = LoggerFactory.getLogger(RSAUtils.class);

    public static String signPayload(String privateKeyString, String payload, String keyAlias, String keyPass) throws Exception {
        try {
            PrivateKey privateKey = getPrivateKey(keyPass, keyAlias, privateKeyString);
            Signature ecdsaSign = Signature.getInstance(ALGORITHM);
            ecdsaSign.initSign(privateKey);
            ecdsaSign.update(payload.getBytes(StandardCharsets.UTF_8));
            byte[] signature = ecdsaSign.sign();
            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception var7) {
            log.error("Error: ", var7);
            throw var7;
        }
    }

    public static boolean verifyPayload(String data, String signature, String publicKeyString, String keyAlias, String keyPass) {
        try {
            Signature ecdsaVerifySignature = Signature.getInstance(ALGORITHM);

            PublicKey publicKey = getPublicKey(keyPass, keyAlias, publicKeyString);
            ecdsaVerifySignature.initVerify(publicKey);
            ecdsaVerifySignature.update(data.getBytes(StandardCharsets.UTF_8));
            return ecdsaVerifySignature.verify(Base64.getMimeDecoder().decode(signature));
        } catch (Exception var7) {
            return false;
        }
    }

    private static PrivateKey getPrivateKey(String keyPass, String keyAlias, String privateKeyString) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(PKCS_12);

        byte[] data = Base64.getDecoder().decode(privateKeyString);
        InputStream inputStream = new ByteArrayInputStream(data);

        keyStore.load(inputStream, keyPass.toCharArray());
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPass.toCharArray());
        return privateKey;
    }

    private static PublicKey getPublicKey(String keyPass, String keyAlias, String publicKeyString) throws Exception {

        KeyStore keyStore = KeyStore.getInstance(PKCS_12);

        byte[] data = Base64.getDecoder().decode(publicKeyString);
        InputStream inputStream = new ByteArrayInputStream(data);

        keyStore.load(inputStream, keyPass.toCharArray());
        Certificate cert = keyStore.getCertificate(keyAlias);
        PublicKey publicKey = cert.getPublicKey();
        return publicKey;

    }
}