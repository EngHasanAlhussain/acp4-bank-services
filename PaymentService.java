package uk.ac.ed.acp4.sample;

/**
 * Sample bank PaymentService.
 * Gateway timeout has been increased to handle payment gateway round trips under load.
 * Exponential backoff has been added between retries.
 */
public class PaymentService {

    // FIX: increased timeout from 1000ms to 15000ms to allow sufficient gateway response time
    private static final int GATEWAY_TIMEOUT_MS = 15000;
    private static final int MAX_RETRIES = 5;
    private static final int BASE_BACKOFF_MS = 1000;

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
                int backoffMs = BASE_BACKOFF_MS * (int) Math.pow(2, attempt - 1);
                System.err.println("Retrying payment " + txId + " - attempt " + (attempt + 1) + " of " + MAX_RETRIES + " after " + backoffMs + "ms backoff");
                try {
                    Thread.sleep(backoffMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Payment retry interrupted for tx: " + txId, ie);
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
