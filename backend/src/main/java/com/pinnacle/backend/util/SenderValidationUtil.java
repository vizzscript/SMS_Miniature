package com.pinnacle.backend.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SenderValidationUtil {
    public static ResponseEntity<?> validate(String sender) {
        if (sender == null || sender.trim().isEmpty()) {
            // throw new IllegalArgumentException("Sender ID cannot be empty.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Sender ID cannot be empty.");
        }

        // Allow alphanumeric, underscores, and hyphens. Length: 3 to 11 characters.
        String regex = "^[A-Za-z][A-Za-z0-9_-]{2,10}$";

        if (!sender.matches(regex)) {
            // throw new IllegalArgumentException(
            // "Invalid Sender ID. It must be 3 to 11 characters, alphanumeric, and start
            // with a letter. Allowed special characters: '_' and '-'.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    "Invalid Sender ID. It must be 3 to 11 characters, alphanumeric, and start with a letter. Allowed special characters: '_' and '-'.");
        }
        return ResponseEntity.ok("Sender is valid!!");
    }
}

/*
 * Sender ID Valid? Reason
 * ABC123 ✅ Yes Alphanumeric, starts with a letter
 * CP-SJS ✅ Yes Hyphen allowed
 * SMS_India ✅ Yes Underscore allowed
 * SMS-India ✅ Yes Hyphen allowed
 * 123ABC ❌ No Cannot start with a number
 * 
 * @ABC123 ❌ No Special characters (@) are not allowed
 * A ❌ No Less than 3 characters
 * ABCDEFGHIJKLMNOP ❌ No More than 11 characters
 */