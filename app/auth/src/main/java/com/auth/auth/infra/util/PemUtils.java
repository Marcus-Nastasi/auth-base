package com.auth.auth.infra.util;

import com.auth.core.exceptions.InternalException;
import com.auth.core.shared.Errors;
import com.auth.core.shared.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemUtils {

    private static final String LOG_CODE = "PEM-UTILS";

    private PemUtils() {}

    public static RSAPrivateKey readPrivateKey(final String filename) throws Exception {
        final Path path = extractPath(filename);
        final byte[] file = getFile(path);
        final String key = extractBeginAndEnd(file);

        byte[] bytes = null;
        try {
            bytes = Base64.getDecoder().decode(key);
        } catch (final IllegalArgumentException e) {
            Logger.error(LOG_CODE, "Error decoding the key with Base64", key, e);
        }

        return RSAPrivateKey.class.cast(generateRSAKey(bytes, true));
    }

    public static RSAPublicKey readPublicKey(final String filename) throws Exception {
        final Path path = extractPath(filename);
        final byte[] file = getFile(path);
        final String key = extractBeginAndEnd(file);

        byte[] bytes = null;
        try {
            bytes = Base64.getDecoder().decode(key);
        } catch (final IllegalArgumentException e) {
            Logger.error(LOG_CODE, "Error decoding the key with Base64", key, e);
        }

        return RSAPublicKey.class.cast(generateRSAKey(bytes, false));
    }

    private static Path extractPath(final String filename) throws InternalException {
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
            throw new InternalException("I/O fail reading bytes from file: " + path.getFileName(), e);
        } catch (OutOfMemoryError e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Out of memory reading bytes from file: " + path.getFileName(), e);
        } catch (SecurityException e) {
            Logger.error(LOG_CODE, e.getMessage(), null, e);
            throw new InternalException("Forbidden: access exception, file not reachable", e);
        }
    }

    private static String extractBeginAndEnd(final byte[] file) {
        return new String(file)
            .replaceAll("-----BEGIN (.*)-----", "")
            .replaceAll("-----END (.*)-----", "")
            .replaceAll("\\s", "");
    }

    private static RSAKey generateRSAKey(final byte[] bytes, final boolean isPrivate) throws InternalException {
        try {
            if (bytes == null) {
                Logger.error(LOG_CODE, "Byte array is null while generating key");
                throw new InternalException("Byte array is null while generating key");
            }

            final KeyFactory kf = KeyFactory.getInstance("RSA");

            if (isPrivate) {
                try {
                    final PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);

                    final RSAPrivateKey key = RSAPrivateKey.class.cast(kf.generatePrivate(spec));
                    Logger.info(LOG_CODE, "Success generating private key");

                    return key;
                } catch (final InvalidKeySpecException e) {
                    Logger.error(LOG_CODE, Errors.INVALID_PRIVATE_KEY.getMsg());
                    throw new InternalException(Errors.INVALID_PRIVATE_KEY, e);
                }
            } else {
                try {
                    final X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);

                    final RSAPublicKey key = RSAPublicKey.class.cast(kf.generatePublic(spec));
                    Logger.info(LOG_CODE, "Success generating public key");

                    return key;
                } catch (final InvalidKeySpecException e) {
                    Logger.error(LOG_CODE, Errors.INVALID_PUBLIC_KEY.getMsg());
                    throw new InternalException(Errors.INVALID_PUBLIC_KEY, e);
                }
            }
        } catch (final NoSuchAlgorithmException e) {
            Logger.error(LOG_CODE, "The algorithm specified on KeyFactory is not supported", e.getMessage(), e);
            return null;
        }
    }
}
