package com.inboop.backend.instagram.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Utility for creating and verifying signed cookies.
 * Used to securely pass user ID through OAuth flow.
 */
@Component
public class SecureCookieUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String SEPARATOR = ".";

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Create a signed value: base64(value).base64(hmac(value))
     */
    public String sign(String value) {
        try {
            String encodedValue = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(value.getBytes(StandardCharsets.UTF_8));
            String signature = computeHmac(encodedValue);
            return encodedValue + SEPARATOR + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to sign value", e);
        }
    }

    /**
     * Verify and extract the original value from a signed string.
     * Returns null if verification fails.
     */
    public String verify(String signedValue) {
        if (signedValue == null || !signedValue.contains(SEPARATOR)) {
            return null;
        }

        try {
            String[] parts = signedValue.split("\\" + SEPARATOR, 2);
            if (parts.length != 2) {
                return null;
            }

            String encodedValue = parts[0];
            String providedSignature = parts[1];
            String expectedSignature = computeHmac(encodedValue);

            if (!constantTimeEquals(expectedSignature, providedSignature)) {
                return null;
            }

            return new String(Base64.getUrlDecoder().decode(encodedValue), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private String computeHmac(String data) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256);
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
        mac.init(keySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hmacBytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
