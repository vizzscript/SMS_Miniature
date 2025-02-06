package com.pinnacle.backend.util;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
// import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import com.pinnacle.backend.exceptions.HeaderValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeaderValidationUtil {

    private static final int MAX_CONTENT_LENGTH = 520000; // 50 KB
    private static final String[] ALLOWED_RESPONSE_TYPES = { "json", "xml" };

    public static ResponseEntity<?> validateHeaders(HttpHeaders headers) {
        try {
            validateResponseType(headers.getFirst("responsetype"));
            validateIsEncrypted(headers.getFirst("isencrypted"));
            validateContentLength(headers.getFirst("content-length"));
            return ResponseEntity.ok("Headers fetched successfully!!");
        } catch (HeaderValidationException e) {
            log.error("Header validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    private static ResponseEntity<?> validateResponseType(String responseType) {
        if (responseType == null || !List.of(ALLOWED_RESPONSE_TYPES).contains(responseType.toLowerCase())) {
            log.warn("Invalid responsetype: {}. Allowed values: json, xml.", responseType);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid responsetype. Allowed values: json, xml.");
        }
        return ResponseEntity.ok("Response type is valid.");
    }

    private static ResponseEntity<?> validateIsEncrypted(String isEncrypted) {
        if (isEncrypted == null || (!isEncrypted.equals("0") && !isEncrypted.equals("1"))) {
            // throw new HeaderValidationException("Invalid isencrypted value. Allowed
            // values: 0 (No), 1 (Yes).");
            log.warn("Invalid isencrypted value. Allowed values: 0(No), 1(Yes).");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid isencrypted value. Allowed values: 0(No), 1(Yes)");
        }
        return ResponseEntity.ok("isEncrpted is valid.");
    }

    public static ResponseEntity<?> validateContentLength(String contentLengthStr) {
        if (contentLengthStr == null) {
            // throw new HeaderValidationException("content-length header is missing.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("content-length header is missing.");
        }

        try {
            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength > MAX_CONTENT_LENGTH) {
                // throw new HeaderValidationException(
                // "Payload size exceeds the allowed limit of " + MAX_CONTENT_LENGTH + "
                // bytes.");
                return ResponseEntity.status(429)
                        .body("Payload size exceeds the allowed limit of " + MAX_CONTENT_LENGTH + " bytes.");

            }
        } catch (NumberFormatException e) {
            // throw new HeaderValidationException("Invalid content-length value.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid content-length value.");
        }
        return ResponseEntity.ok("Content length is valid.");
    }
}

/*
 * ✅ Example Cases
 * Header Key Example Value Valid? Reason
 * responsetype "json" ✅ Yes Allowed value
 * responsetype "xml" ✅ Yes Allowed value
 * responsetype "text" ❌ No Only json or xml are allowed
 * isencrypted "1" ✅ Yes Indicates encrypted payload
 * isencrypted "0" ✅ Yes Indicates unencrypted payload
 * isencrypted "yes" ❌ No Only "0" or "1" are allowed
 * content-length "45000" ✅ Yes Within limit (50 KB)
 * content-length "60000" ❌ No Exceeds 50 KB limit
 * content-length "invalid" ❌ No Must be a numeric value
 */