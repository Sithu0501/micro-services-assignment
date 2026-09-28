package com.ridelink.accountservice.util;

import java.util.regex.Pattern;

/**
 * Utility methods for string sanitization, email normalization, and Sri Lankan phone number validation.
 */
public final class ValidationUtils {

    /**
     * Regex pattern supporting Sri Lankan mobile phone formats:
     * - International with +: +947XXXXXXXX (e.g. +94771234567)
     * - International without +: 947XXXXXXXX (e.g. 94771234567)
     * - Domestic with leading zero: 07XXXXXXXX (e.g. 0771234567)
     * - Domestic without leading zero: 7XXXXXXXX (e.g. 771234567)
     */
    public static final String SRI_LANKAN_PHONE_REGEX = "^(\\+94|94|0)?7[0-9]{8}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(SRI_LANKAN_PHONE_REGEX);

    private ValidationUtils() {
        // Prevent instantiation
    }

    /**
     * Normalizes an email address to trimmed lowercase.
     *
     * @param email input email
     * @return normalized lowercase email, or null if input was null
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    /**
     * Validates whether a given phone string conforms to Sri Lankan mobile standards.
     *
     * @param phone phone number string
     * @return true if valid Sri Lankan mobile, false otherwise
     */
    public static boolean isValidSriLankanPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        String clean = phone.replaceAll("[\\s\\-]", "");
        return PHONE_PATTERN.matcher(clean).matches();
    }

    /**
     * Normalizes a Sri Lankan mobile number into the canonical E.164 international format (+947XXXXXXXX).
     *
     * @param phone raw phone number
     * @return normalized phone number formatted as +947XXXXXXXX
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String clean = phone.replaceAll("[\\s\\-]", "");
        if (clean.startsWith("+94")) {
            return clean;
        } else if (clean.startsWith("94")) {
            return "+" + clean;
        } else if (clean.startsWith("0")) {
            return "+94" + clean.substring(1);
        } else if (clean.startsWith("7") && clean.length() == 9) {
            return "+94" + clean;
        }
        return clean;
    }
}
