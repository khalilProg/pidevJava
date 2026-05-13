package tn.esprit.tools;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class to hash passwords in a way that is compatible with Symfony's
 * native BCrypt password hasher.
 *
 * Symfony uses the $2y$ BCrypt prefix, while Java's jBCrypt library produces
 * the $2a$ prefix. Both are functionally identical, but Symfony expects $2y$.
 *
 * This class centralises hashing so every part of the desktop app writes
 * Symfony-compatible hashes to the shared database.
 */
public final class SymfonyPasswordEncoder {

    /** Default BCrypt cost factor – matches Symfony's default of 13. */
    private static final int COST = 13;

    private SymfonyPasswordEncoder() {
        // utility class
    }

    /**
     * Hash a plain-text password using BCrypt with the $2y$ prefix so the
     * resulting hash is directly usable by Symfony's password_verify().
     */
    public static String hash(String plainPassword) {
        // jBCrypt produces $2a$… – replace with $2y$ for Symfony compatibility
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt(COST));
        return "$2y$" + hash.substring(4);
    }

    /**
     * Verify a plain-text password against a stored hash.
     * Supports both $2y$ (Symfony) and $2a$ (jBCrypt) prefixes.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null) {
            return false;
        }
        // Normalise to $2a$ so jBCrypt can verify it
        String normalised = storedHash;
        if (normalised.startsWith("$2y$")) {
            normalised = "$2a$" + normalised.substring(4);
        }
        return BCrypt.checkpw(plainPassword, normalised);
    }
}
