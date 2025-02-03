package com.pinnacle.backend.util;

public class MessageValidationUtil {
    private static final int MIN_LENGTH = 1;
    private static final int MAX_LENGTH = 160; // Adjust based on requirements

    public static void validate(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty.");
        }

        if (message.length() < MIN_LENGTH || message.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Message length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters.");
        }

        // Optional: Restrict certain symbols if required (adjust regex as needed)
        // String forbiddenPattern = "[<>\\[\\]{}|^]"; // Example: Disallow <, >, [, ],
        // {, }, |
        // if (message.matches(".*" + forbiddenPattern + ".*")) {
        // throw new IllegalArgumentException("Message contains restricted
        // characters.");
        // }
    }
}

/*
 *  Message                                        Valid?                    Reason
 *  "Hello, how are you?"                          ✅ Yes            Within length limits
 *  ""                                             ❌ No             Empty message
 *  " "                                            ❌ No             Whitespace-only message
 *  "This is a test message
 *  exceeding 160                                  ❌ No             Too long
 *  characters...............
 *  (truncated)................"
 *  "Alert: Your OTP is 1234"                      ✅ Yes            Valid content
 *  "Special symbols <> are not allowed"           ❌ No             Contains < and >
 *  "😊 Unicode characters allowed"                ✅ Yes            Unicode allowed
 */
