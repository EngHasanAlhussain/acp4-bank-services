package uk.ac.ed.acp4.sample;

/**
 * Sample bank AuthService - loads JWT secret from environment variable.
 * The secret must be configured externally and rotated periodically.
 */
public class AuthService {

    // FIX: Load secret from environment variable instead of hardcoding
    private final String jwtSecret;

    public AuthService() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not set. "
                + "Please configure a strong secret key."
            );
        }
        this.jwtSecret = secret;
    }

    // Visible for testing: allow injecting the secret
    AuthService(String jwtSecret) {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalArgumentException("JWT secret must not be null or blank");
        }
        this.jwtSecret = jwtSecret;
    }

    public String generateToken(String userId) {
        // In real code this would use a JWT library
        return sign(userId + ":token", jwtSecret);
    }

    public boolean verifyToken(String token, String userId) {
        try {
            String expected = sign(userId + ":token", jwtSecret);
            if (!expected.equals(token)) {
                throw new JWTVerificationException(
                        "Token signature invalid for user " + userId
                );
            }
            return true;
        } catch (JWTVerificationException e) {
            throw e;
        } catch (Exception e) {
            throw new JWTVerificationException("Unexpected error verifying token: " + e.getMessage());
        }
    }

    private String sign(String payload, String secret) {
        // Simplified - real impl would use HMAC-SHA256
        return Integer.toHexString((payload + secret).hashCode());
    }

    public static class JWTVerificationException extends RuntimeException {
        public JWTVerificationException(String message) {
            super(message);
        }
    }
}
