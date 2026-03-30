package uk.ac.ed.acp4.sample;

/**
 * Sample bank PaymentService.
 * Gateway timeout has been increased to 5000ms to accommodate payment gateway
 * round-trip latency under normal and peak load conditions.
 * Exponential backoff added between retries to reduce pressure on the gateway.
 */
public class PaymentService {

    // FIX: Increased timeout from 1000ms to 5000ms to allow sufficient gateway response time
    private static final int GATEWAY_TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_BACKOFF_MS = 500;

    public PaymentResult processPayment(String txId, String userId, double amount) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                attempt++;
                return callPaymentGateway(txId, amount, GATEWAY_TIMEOUT_MS);
            } catch (PaymentGatewayTimeoutException e) {
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException(
                            "Payment " + txId + " permanently failed for user " + userId +
                                    " after max retries"
                    );
                }
                System.err.println("Retrying payment " + txId + " - attempt " + (attempt + 1) + " of " + MAX_RETRIES);
                // Exponential backoff before retrying
                try {
                    long backoff = INITIAL_BACKOFF_MS * (long) Math.pow(2, attempt - 1);
                    Thread.sleep(backoff);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted for tx: " + txId, ie);
                }
            }
        }
        throw new RuntimeException("Unexpected retry loop exit for tx: " + txId);
    }

    private PaymentResult callPaymentGateway(String txId, double amount, int timeoutMs) {
        // Simulated gateway call - throws if gateway doesn't respond in time
        throw new PaymentGatewayTimeoutException(
                "No response from gateway after " + timeoutMs + "ms for tx " + txId
        );
    }

    public static class PaymentGatewayTimeoutException extends RuntimeException {
        public PaymentGatewayTimeoutException(String message) {
            super(message);
        }
    }

    public static class PaymentResult {
        private final String txId;
        private final String status;
        public PaymentResult(String txId, String status) {
            this.txId = txId;
            this.status = status;
        }
    }
}
