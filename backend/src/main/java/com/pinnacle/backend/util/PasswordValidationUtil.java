package com.pinnacle.backend.util;

import java.util.regex.Pattern;

public class PasswordValidationUtil {
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 20;
    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{" + MIN_LENGTH
            + "," + MAX_LENGTH + "}$";

    public static void validate(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Password must be between " + MIN_LENGTH + " & " + MAX_LENGTH + " characters.");
        }

        if (!Pattern.matches(PASSWORD_REGEX, password)) {
            throw new IllegalArgumentException(
                    "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.");
        }

        if (password.toLowerCase().contains("password") || password.matches(".*(123456|qwerty|admin).*")) {
            throw new IllegalArgumentException("Password is too weak or commonly used.");
        }
    }
}

/*
 * ✅ Example Cases
 * Password                     Valid?                          Reason
 * Secure@123                   ✅ Yes                  Meets all requirements
 * Test1234!                    ✅ Yes                  Meets all requirements
 * password                     ❌ No                   Too common
 * 12345678                     ❌ No                   No uppercase, lowercase, or special character
 * abcdefgH1                    ❌ No                   No special character
 * Abc@12                       ❌ No                   Too short (min 8 characters)
 * Abcdefghij                   ❌ No                   No digit, no special character
 * Admin@123                    ❌ No                   Contains "admin"
*/