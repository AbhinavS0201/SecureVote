package com.securevote.crypto;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

/**
 * Cryptographic utility belt for the Secure Voting System.
 *
 * Provides:
 *  1. SHA-256 hashing           — block linking, voter ID anonymisation
 *  2. PBKDF2 password hashing   — secure credential storage
 *  3. RSA digital signatures    — vote authenticity / non-repudiation
 *  4. HMAC-SHA256               — session token integrity
 */
public final class CryptoUtils {

    // ── RSA key pair (in production: load from a secure KeyStore) ─────────────
    private static KeyPair RSA_KEY_PAIR;

    static {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048, new SecureRandom());
            RSA_KEY_PAIR = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new ExceptionInInitializerError("RSA key generation failed: " + e.getMessage());
        }
    }

    private CryptoUtils() {}

    // ── 1. SHA-256 ─────────────────────────────────────────────────────────────

    /**
     * Returns the HEX-encoded SHA-256 digest of the input string.
     * Used to: chain blocks, anonymise voter IDs.
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }

    // ── 2. PBKDF2 Password Hashing ─────────────────────────────────────────────

    private static final int PBKDF2_ITERATIONS = 310_000;
    private static final int KEY_LENGTH_BITS   = 256;
    private static final int SALT_BYTES        = 16;

    /**
     * Hashes a password using PBKDF2-HMAC-SHA256 with a random salt.
     * @return  "salt$hash" where both parts are Base64-encoded.
     */
    public static String hashPassword(String password) {
        try {
            byte[] salt = new byte[SALT_BYTES];
            new SecureRandom().nextBytes(salt);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();

            return Base64.getEncoder().encodeToString(salt)
                 + "$"
                 + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed", e);
        }
    }

    /**
     * Verifies a plain-text password against the stored PBKDF2 token.
     */
    public static boolean verifyPassword(String password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            byte[] salt     = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS
            );
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actual = skf.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(expected, actual); // constant-time compare
        } catch (Exception e) {
            return false;
        }
    }

    // ── 3. RSA Digital Signatures ──────────────────────────────────────────────

    /**
     * Signs the given data with the server's RSA private key.
     * Provides authenticity and non-repudiation for each vote.
     */
    public static String signData(String data) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(RSA_KEY_PAIR.getPrivate(), new SecureRandom());
            signer.update(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception e) {
            throw new RuntimeException("Signing failed", e);
        }
    }

    /**
     * Verifies an RSA signature against data using the server's public key.
     */
    public static boolean verifySignature(String data, String signatureB64) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(RSA_KEY_PAIR.getPublic());
            verifier.update(data.getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(signatureB64));
        } catch (Exception e) {
            return false;
        }
    }

    // ── 4. HMAC-SHA256 (Session Tokens) ────────────────────────────────────────

    /**
     * Generates a cryptographically random 32-byte session token (Base64-encoded).
     */
    public static String generateSessionToken() {
        byte[] token = new byte[32];
        new SecureRandom().nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    // ── Internal helpers ───────────────────────────────────────────────────────

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
