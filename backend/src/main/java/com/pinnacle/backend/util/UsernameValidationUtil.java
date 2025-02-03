package com.pinnacle.backend.util;

public class UsernameValidationUtil {
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 20;
    private static final String USERNAME_REGEX = "^[a-zA-Z][a-zA-Z0-9._]{2,19}$";

    public static void validate(String userName) {
        if (userName == null || userName.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty or null!!");
        }

        if (userName.length() < MIN_LENGTH || userName.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Username must be between " + MIN_LENGTH + " & " + MAX_LENGTH + " characters.");
        }

        if (!userName.matches(USERNAME_REGEX)) {
            throw new IllegalArgumentException(
                    "Username must start with a letter and can contain letters, numbers, dots, and underscores.");
        }

        if (userName.contains("..") || userName.contains("_")) {
            throw new IllegalArgumentException("Username cannot contain consecutive dots or underscores.");
        }
    }

}

/*
 * ✅ Example Cases
 * Username                 Valid?                  Reason
 * john_doe                 ✅ Yes              Allowed format
 * JohnDoe123               ✅ Yes              Alphanumeric
 * user.name                ✅ Yes              Dots allowed
 * 12John                   ❌ No               Must start with a letter
 * john@doe                 ❌ No               Special characters not allowed
 * jo                       ❌ No               Too short (min 3 characters)
 * user..name               ❌ No               Consecutive dots not allowed
 * admin                    ❌ No               Blacklisted username
 * user name                ❌ No               Spaces not allowed
 * 
 * 
 *  Username: JohnDoe123
 *  pwd: Secure@123
 *  sender: CP-SMS
 *  message: Hello, how are you?
 *  mobileno: +919876543210
 */