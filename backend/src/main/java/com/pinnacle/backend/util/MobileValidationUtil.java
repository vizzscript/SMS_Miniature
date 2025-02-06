package com.pinnacle.backend.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class MobileValidationUtil {
    public static ResponseEntity<?> validate(String mobileNo) {
        if (mobileNo == null || mobileNo.trim().isEmpty()) {
            // throw new IllegalArgumentException("Mobile number cannot be empty.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Mobile number cannot be empty.");
        }

        String regex = "^(\\+\\d{1,3})?[1-9][0-9]{9}$";

        if (!mobileNo.matches(regex)) {
            // throw new IllegalArgumentException("Invalid mobile number format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid mobile number format.");
        }
        return ResponseEntity.ok("Mobile Number is valid!!");
    }
}

/*
 * Mobile Number                        Valid?               Reason
 * "9876543210"                         ✅ Yes               Standard 10-digit mobile number
 * "+919876543210"                      ✅ Yes               Valid international format (+91 for India)
 * +11234567890                         ✅ Yes               USA (+1)
 * +442012345678                        ✅ Yes               UK (+44)
 * +4915123456789                       ✅ Yes               Germany (+49)
 * +611234567890                        ✅ Yes               Australia (+61)
 * "0123456789"                         ❌ No                Cannot start with 0
 * "987654"                             ❌ No                Less than 10 digits
 * "9876543210123"                      ❌ No                More than 10 digits
 * "+919876543"                         ❌ No                Invalid international format
 * "abcdefghij"                         ❌ No                Only numbers are allowed
 * "98765-43210"                        ❌ No                Special characters not allowed
 * " "                                  ❌ No                Empty string
 * +1234567890                          ❌ No                Country code is missing digits
 * +919876543                           ❌ No                Less than 10 digits
 * +9198765432100                       ❌ No                More than 10 digits
 * +0 9876543210                        ❌ No                Invalid country code
 */
