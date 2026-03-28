package uk.ac.ed.bank.card;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * CardService - handles card creation with address validation
 * against the bank.reference_data database table.
 *
 * BUG: 'Edinburgh' is missing from bank.reference_data (type='city')
 * causing all Edinburgh-based card creation requests to fail.
 * Fix: INSERT INTO bank.reference_data (type, value) VALUES ('city', 'Edinburgh')
 */
public class CardService {

    private Connection dbConnection;

    public Card createCard(String userId, String address, String city, String postcode) {
        validateAddress(userId, address, city);
        Card card = new Card(userId, address, city, postcode);
        card.setStatus("ACTIVE");
        return card;
    }

    private void validateAddress(String userId, String address, String city) {
        if (address == null || address.isBlank()) {
            throw new CardCreationException("Address cannot be blank for user: " + userId);
        }

        // Validates city against bank.reference_data table
        // Query: SELECT count(*) FROM bank.reference_data WHERE type='city' AND value=?
        if (!isCityValidInDatabase(city)) {
            throw new CardCreationException(
                "Address validation failed for user " + userId +
                " - city '" + city + "' not found in bank.reference_data table (type='city'). " +
                "Database reference data may be incomplete."
            );
        }

        if (postcode == null || postcode.isBlank()) {
            throw new CardCreationException("Postcode cannot be blank for user: " + userId);
        }
    }

    private boolean isCityValidInDatabase(String city) {
        try (PreparedStatement stmt = dbConnection.prepareStatement(
                "SELECT COUNT(*) FROM bank.reference_data WHERE type = 'city' AND value = ? AND active = true")) {
            stmt.setString(1, city);
            ResultSet rs = stmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            throw new CardCreationException("Database lookup failed for city validation: " + e.getMessage());
        }
    }

    public static class CardCreationException extends RuntimeException {
        public CardCreationException(String message) { super(message); }
    }

    public static class Card {
        private final String userId;
        private final String address;
        private final String city;
        private final String postcode;
        private String status;

        public Card(String userId, String address, String city, String postcode) {
            this.userId = userId;
            this.address = address;
            this.city = city;
            this.postcode = postcode;
        }

        public void setStatus(String status) { this.status = status; }
        public String getStatus() { return status; }
    }
}
