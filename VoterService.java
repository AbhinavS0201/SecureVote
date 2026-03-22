package com.securevote.service;

import com.securevote.crypto.CryptoUtils;
import com.securevote.model.Voter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages voter registration, authentication, and session lifecycle.
 *
 * Security controls:
 *  - Passwords hashed with PBKDF2 before storage
 *  - Duplicate registration rejected
 *  - Session tokens bound to voter ID (constant-time lookup)
 *  - Login attempt throttling (max 5 per voter)
 */
public class VoterService {

    private static VoterService instance;
    public static synchronized VoterService getInstance() {
        if (instance == null) instance = new VoterService();
        return instance;
    }

    private final Map<String, Voter>   voterStore     = new ConcurrentHashMap<>();
    private final Map<String, String>  sessionStore   = new ConcurrentHashMap<>(); // token → voterId
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private VoterService() {
        // Seed a default admin account
        registerVoter("ADMIN001", "System Admin", "admin@securevote.gov", "Admin@1234");
        Voter admin = voterStore.get("ADMIN001");
        if (admin != null) admin.setRole("ADMIN");
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Registers a new voter.
     * @throws IllegalArgumentException if the voter ID is already registered
     *         or if the password does not meet policy.
     */
    public Voter registerVoter(String voterId, String name, String email, String password) {
        if (voterStore.containsKey(voterId)) {
            throw new IllegalArgumentException("Voter ID already registered: " + voterId);
        }
        validatePassword(password);

        String hash  = CryptoUtils.hashPassword(password);
        Voter  voter = new Voter(voterId, name, email, hash);
        voterStore.put(voterId, voter);
        return voter;
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Authenticates a voter and returns a session token on success.
     * @throws SecurityException after MAX_FAILED_ATTEMPTS consecutive failures.
     */
    public String login(String voterId, String password) {
        int attempts = failedAttempts.getOrDefault(voterId, 0);
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            throw new SecurityException("Account locked after " + MAX_FAILED_ATTEMPTS
                + " failed attempts. Contact administrator.");
        }

        Voter voter = voterStore.get(voterId);
        if (voter == null || !CryptoUtils.verifyPassword(password, voter.getPasswordHash())) {
            failedAttempts.merge(voterId, 1, Integer::sum);
            throw new SecurityException("Invalid credentials.");
        }

        failedAttempts.remove(voterId); // reset on success
        String token = CryptoUtils.generateSessionToken();
        sessionStore.put(token, voterId);
        return token;
    }

    public void logout(String token) {
        sessionStore.remove(token);
    }

    // ── Session resolution ────────────────────────────────────────────────────

    public Voter getVoterByToken(String token) {
        String voterId = sessionStore.get(token);
        return (voterId == null) ? null : voterStore.get(voterId);
    }

    public Voter getVoterById(String voterId) {
        return voterStore.get(voterId);
    }

    public Collection<Voter> getAllVoters() {
        return voterStore.values();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        boolean hasUpper  = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit  = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
        if (!hasUpper || !hasDigit || !hasSymbol) {
            throw new IllegalArgumentException(
                "Password must contain an uppercase letter, a digit, and a special character.");
        }
    }
}
