package com.pinnacle.backend.util;

import java.util.List;

import org.springframework.http.HttpHeaders;

public class HeaderValidationUtil {

    private static final int MAX_CONTENT_LENGTH = 512000; // 50 KB
    private static final String[] ALLOWED_RESPONSE_TYPES = { "json", "xml" };

    public static void validateHeaders(HttpHeaders headers) {
        validateResponseType(headers.getFirst("responsetype"));
        validateIsEncrypted(headers.getFirst("isencrypted"));
        validateContentLength(headers.getFirst("content-length"));
    }

    private static void validateResponseType(String responseType) {
        if (responseType == null || !List.of(ALLOWED_RESPONSE_TYPES).contains(responseType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid responsetype. Allowed values: json, xml.");
        }
    }

    private static void validateIsEncrypted(String isEncrypted) {
        if (isEncrypted == null || (!isEncrypted.equals("0") && !isEncrypted.equals("1"))) {
            throw new IllegalArgumentException("Invalid isencrypted value. Allowed values: 0 (No), 1 (Yes).");
        }
    }

    private static void validateContentLength(String contentLengthStr) {
        if (contentLengthStr == null) {
            throw new IllegalArgumentException("content-length header is missing.");
        }

        try {
            int contentLength = Integer.parseInt(contentLengthStr);
            if (contentLength > MAX_CONTENT_LENGTH) {
                throw new IllegalArgumentException(
                        "Payload size exceeds the allowed limit of " + MAX_CONTENT_LENGTH + " bytes.");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid content-length value.");
        }
    }
}

/*
 * ✅ Example Cases
 * Header Key                       Example Value           Valid?              Reason
 * responsetype                         "json"              ✅ Yes          Allowed value
 * responsetype                         "xml"               ✅ Yes          Allowed value
 * responsetype                         "text"              ❌ No           Only json or xml are allowed
 * isencrypted                          "1"                 ✅ Yes          Indicates encrypted payload
 * isencrypted                          "0"                 ✅ Yes          Indicates unencrypted payload
 * isencrypted                          "yes"               ❌ No           Only "0" or "1" are allowed
 * content-length                       "45000"             ✅ Yes          Within limit (50 KB)
 * content-length                       "60000"             ❌ No           Exceeds 50 KB limit
 * content-length                       "invalid"           ❌ No           Must be a numeric value
 */