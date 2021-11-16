package tz.go.moh.him.thscp.mediator.muse.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {
    public static final String FACTORY_TYPE = "RSA";
    public static final String ALGORITHM = "SHA256withRSA";
    public static final String EC_CURVE_NAME = "secp256k1";
    private static final Logger log = LoggerFactory.getLogger(RSAUtils.class);

    public static String signPayload(String payload, String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, UnsupportedEncodingException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(FACTORY_TYPE);
            PKCS8EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString));
            PrivateKey privateKey = keyFactory.generatePrivate(encodedKeySpec);
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

    public static boolean verifyPayload(String data, String signature, String publicKeyString) {
        try {
            Signature ecdsaVerifySignature = Signature.getInstance(ALGORITHM);
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyString));
            KeyFactory keyFactory = KeyFactory.getInstance(FACTORY_TYPE);
            PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
            ecdsaVerifySignature.initVerify(publicKey);
            ecdsaVerifySignature.update(data.getBytes(StandardCharsets.UTF_8));
            return ecdsaVerifySignature.verify(Base64.getMimeDecoder().decode(signature));
        } catch (Exception var7) {
            return false;
        }
    }
}