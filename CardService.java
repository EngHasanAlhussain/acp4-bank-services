package uk.ac.ed.acp4.sample;

import java.util.List;

/**
 * Sample bank CardService - intentionally contains a bug
 * where city validation fails for unlisted cities.
 * This file is read by the AI agent to generate fix suggestions.
 */
public class CardService {

    private static final List<String> VALID_CITIES = List.of(
            "London", "Manchester", "Birmingham", "Glasgow", "Leeds",
            "Liverpool", "Bristol", "Sheffield", "Bradford", "Newcastle"
            // BUG: "Edinburgh" is missing from this list
    );

    public Card createCard(String userId, String address, String city, String postcode) {
        validateAddress(userId, address, city, postcode);
        Card card = new Card(userId, address, city, postcode);
        card.setStatus("ACTIVE");
        return card;
    }

    private void validateAddress(String userId, String address, String city, String postcode) {
        if (address == null || address.isBlank()) {
            throw new CardCreationException("Address cannot be blank for user: " + userId);
        }
        // Throws if city not in reference list
        if (!VALID_CITIES.contains(city)) {
            throw new CardCreationException(
                    "Address validation failed for user " + userId +
                            " - city '" + city + "' not found in reference table"
            );
        }
        if (postcode == null || postcode.isBlank()) {
            throw new CardCreationException("Postcode cannot be blank for user: " + userId);
        }
    }

    public static class CardCreationException extends RuntimeException {
        public CardCreationException(String message) {
            super(message);
        }
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
