package uk.ac.ed.acp4.sample;

/**
 * Sample bank AuthService - loads JWT secret from environment variable
 * and supports key rotation.
 */
public class AuthService {

    // FIX: Load secret from environment variable instead of hardcoding
    private static final String ENV_JWT_SECRET = "JWT_SECRET";

    private String getJwtSecret() {
        String secret = System.getenv(ENV_JWT_SECRET);
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "JWT_SECRET environment variable is not set. "
                + "Please configure a strong secret key and ensure regular rotation."
            );
        }
        return secret;
    }

    public String generateToken(String userId) {
        // In real code this would use a JWT library
        return sign(userId + ":token", getJwtSecret());
    }

    public boolean verifyToken(String token, String userId) {
        try {
            String expected = sign(userId + ":token", getJwtSecret());
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
