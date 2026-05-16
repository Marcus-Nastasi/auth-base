package com.auth.auth.infra.util;

import com.auth.core.exceptions.InternalException;
import com.auth.core.shared.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {

    private static final String LOG_CODE = "PEM-UTILS";

    private PemUtils() {}

    public static RSAPrivateKey readPrivateKey(final String filename) throws Exception {
        final Path path = extractPath(filename);
        final byte[] file = getFile(path);

        String key = new String(file);
        key = key.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        final byte[] bytes = Base64.getDecoder().decode(key);

        final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    public static RSAPublicKey readPublicKey(final String filename) throws Exception {
        final Path path = extractPath(filename);
        final byte[] file = getFile(path);

        String key = new String(file);
        key = key.replaceAll("-----BEGIN (.*)-----", "")
                .replaceAll("-----END (.*)-----", "")
                .replaceAll("\\s", "");

        final byte[] bytes = Base64.getDecoder().decode(key);

        final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        final KeyFactory kf = KeyFactory.getInstance("RSA");

        return (RSAPublicKey) kf.generatePublic(spec);
    }

    private static Path extractPath(final String filename) {
        try {
            return Paths.get(filename);
        } catch (InvalidPathException e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Failed finding path to private's file with name: " + filename, e);
        }
    }

    private static byte[] getFile(final Path path) {
        if (path == null) {
            Logger.error(LOG_CODE, "Path cannot be null");
            throw new InternalException("Path cannot be null");
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Failed reading bytes from file: " + path.getFileName(), e);
        } catch (OutOfMemoryError e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Out of memory reading bytes from file: " + path.getFileName(), e);
        } catch (SecurityException e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Forbidden: access exception, file not reachable", e);
        }
    }
}
