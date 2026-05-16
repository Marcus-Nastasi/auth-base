package com.auth.auth.infra.util;


import com.auth.core.exceptions.InternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

final class PemUtilsTests {

    @TempDir
    Path tempDir;

    private Path privateKeyFile;
    private Path publicKeyFile;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        keyPair = kpg.generateKeyPair();

        final byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        final String privateKeyPem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(privateKeyBytes)
                + "\n-----END PRIVATE KEY-----\n";
        privateKeyFile = tempDir.resolve("private.pem");
        Files.writeString(privateKeyFile, privateKeyPem);

        final byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        final String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKeyBytes)
                + "\n-----END PUBLIC KEY-----\n";
        publicKeyFile = tempDir.resolve("public.pem");
        Files.writeString(publicKeyFile, publicKeyPem);
    }

    // -------------------------------------------------------------------------
    // readPrivateKey — happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldReadPrivateKeySuccessfully() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPrivateKey(privateKeyFile.toString()));
        assertNotNull(result);
        assertInstanceOf(RSAPrivateKey.class, result);
    }

    @Test
    void shouldReadPrivateKeyWithCorrectAlgorithm() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPrivateKey(privateKeyFile.toString()));
        assertEquals("RSA", result.getAlgorithm());
    }

    @Test
    void shouldReadPrivateKeyMatchingOriginalEncoding() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPrivateKey(privateKeyFile.toString()));
        assertArrayEquals(keyPair.getPrivate().getEncoded(), result.getEncoded());
    }

    // -------------------------------------------------------------------------
    // readPublicKey — happy path
    // -------------------------------------------------------------------------

    @Test
    void shouldReadPublicKeySuccessfully() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPublicKey(publicKeyFile.toString()));
        assertNotNull(result);
        assertInstanceOf(RSAPublicKey.class, result);
    }

    @Test
    void shouldReadPublicKeyWithCorrectAlgorithm() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPublicKey(publicKeyFile.toString()));
        assertEquals("RSA", result.getAlgorithm());
    }

    @Test
    void shouldReadPublicKeyMatchingOriginalEncoding() {
        final var result = assertDoesNotThrow(() -> PemUtils.readPublicKey(publicKeyFile.toString()));
        assertArrayEquals(keyPair.getPublic().getEncoded(), result.getEncoded());
    }

    // -------------------------------------------------------------------------
    // readPrivateKey — file not found
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenPrivateKeyFileNotFound() {
        final var nonExistent = tempDir.resolve("ghost_private.pem").toString();
        final var result = assertThrows(InternalException.class, () -> PemUtils.readPrivateKey(nonExistent));
        assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // readPublicKey — file not found
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenPublicKeyFileNotFound() {
        final var nonExistent = tempDir.resolve("ghost_public.pem").toString();
        final var result = assertThrows(InternalException.class, () -> PemUtils.readPublicKey(nonExistent));
        assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // readPrivateKey — invalid path (null-byte triggers InvalidPathException)
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenPrivateKeyPathIsInvalid() {
        final var result = assertThrows(InternalException.class, () -> PemUtils.readPrivateKey("\0invalid_path"));
        assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // readPublicKey — invalid path
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenPublicKeyPathIsInvalid() {
        final var result = assertThrows(InternalException.class, () -> PemUtils.readPublicKey("\0invalid_path"));
        assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // readPrivateKey — malformed PEM content
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenPrivateKeyContentIsNotValidBase64() throws Exception {
        final Path corruptFile = tempDir.resolve("corrupt_private.pem");
        Files.writeString(corruptFile,
                "-----BEGIN PRIVATE KEY-----\n@@not_valid_base64@@\n-----END PRIVATE KEY-----\n");
        assertThrows(Exception.class, () -> PemUtils.readPrivateKey(corruptFile.toString()));
    }

    @Test
    void shouldThrowExceptionWhenPrivateKeyContentIsValidBase64ButNotAKey() throws Exception {
        final Path corruptFile = tempDir.resolve("fake_private.pem");
        final String fakeBase64 = Base64.getEncoder().encodeToString("this is not a real key".getBytes());
        Files.writeString(corruptFile,
                "-----BEGIN PRIVATE KEY-----\n" + fakeBase64 + "\n-----END PRIVATE KEY-----\n");
        assertThrows(Exception.class, () -> PemUtils.readPrivateKey(corruptFile.toString()));
    }

    @Test
    void shouldThrowExceptionWhenPrivateKeyFileIsEmpty() throws Exception {
        final Path emptyFile = tempDir.resolve("empty_private.pem");
        Files.writeString(emptyFile, "");
        assertThrows(Exception.class, () -> PemUtils.readPrivateKey(emptyFile.toString()));
    }

    // -------------------------------------------------------------------------
    // readPublicKey — malformed PEM content
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenPublicKeyContentIsNotValidBase64() throws Exception {
        final Path corruptFile = tempDir.resolve("corrupt_public.pem");
        Files.writeString(corruptFile,
                "-----BEGIN PUBLIC KEY-----\n@@not_valid_base64@@\n-----END PUBLIC KEY-----\n");
        assertThrows(Exception.class, () -> PemUtils.readPublicKey(corruptFile.toString()));
    }

    @Test
    void shouldThrowExceptionWhenPublicKeyContentIsValidBase64ButNotAKey() throws Exception {
        final Path corruptFile = tempDir.resolve("fake_public.pem");
        final String fakeBase64 = Base64.getEncoder().encodeToString("this is not a real key".getBytes());
        Files.writeString(corruptFile,
                "-----BEGIN PUBLIC KEY-----\n" + fakeBase64 + "\n-----END PUBLIC KEY-----\n");
        assertThrows(Exception.class, () -> PemUtils.readPublicKey(corruptFile.toString()));
    }

    @Test
    void shouldThrowExceptionWhenPublicKeyFileIsEmpty() throws Exception {
        final Path emptyFile = tempDir.resolve("empty_public.pem");
        Files.writeString(emptyFile, "");
        assertThrows(Exception.class, () -> PemUtils.readPublicKey(emptyFile.toString()));
    }

    // -------------------------------------------------------------------------
    // Cross-key mismatch — public key file passed to readPrivateKey and vice versa
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowExceptionWhenPublicKeyFileIsPassedToReadPrivateKey() {
        assertThrows(Exception.class, () -> PemUtils.readPrivateKey(publicKeyFile.toString()));
    }

    @Test
    void shouldThrowExceptionWhenPrivateKeyFileIsPassedToReadPublicKey() {
        assertThrows(Exception.class, () -> PemUtils.readPublicKey(privateKeyFile.toString()));
    }


    // -------------------------------------------------------------------------
    // getFile — path == null (branch unreachable via public API; tested via reflection)
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenPathIsNull() throws Exception {
        final Method getFile = PemUtils.class.getDeclaredMethod("getFile", Path.class);
        getFile.setAccessible(true);

        final var thrown = assertThrows(InvocationTargetException.class,
                () -> getFile.invoke(null, (Object) null));

        assertInstanceOf(InternalException.class, thrown.getCause());
        assertEquals("Path cannot be null", thrown.getCause().getMessage());
    }

    // -------------------------------------------------------------------------
    // getFile — catch (OutOfMemoryError) via MockedStatic<Files>
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenOutOfMemoryErrorOccurs() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(any(Path.class)))
                    .thenThrow(new OutOfMemoryError("Simulated OOM"));

            final var result = assertThrows(InternalException.class,
                    () -> PemUtils.readPrivateKey(privateKeyFile.toString()));

            assertNotNull(result);
            assertTrue(result.getMessage().contains("Out of memory reading bytes from file"));
        }
    }

    // -------------------------------------------------------------------------
    // getFile — catch (SecurityException) via MockedStatic<Files>
    // -------------------------------------------------------------------------

    @Test
    void shouldThrowInternalExceptionWhenSecurityExceptionOccurs() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.readAllBytes(any(Path.class)))
                    .thenThrow(new SecurityException("Simulated security denial"));

            final var result = assertThrows(InternalException.class,
                    () -> PemUtils.readPrivateKey(privateKeyFile.toString()));

            assertNotNull(result);
            assertEquals("Forbidden: access exception, file not reachable", result.getMessage());
        }
    }
}
