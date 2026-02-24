package com.auth.auth.infra.util;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {

    private PemUtils() {}

    public static RSAPrivateKey readPrivateKey(final String filename) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filename)));
        key = key.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        final byte[] bytes = Base64.getDecoder().decode(key);

        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    public static RSAPublicKey readPublicKey(final String filename) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filename)));
        key = key.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        final byte[] bytes = Base64.getDecoder().decode(key);

        final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) kf.generatePublic(spec);
    }
}
