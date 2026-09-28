package com.ridelink.accountservice.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils Tests")
class ValidationUtilsTest {

    @Test
    @DisplayName("Email normalization converts to lowercase and trims whitespace")
    void testNormalizeEmail() {
        assertEquals("user@example.com", ValidationUtils.normalizeEmail("  User@Example.COM  "));
        assertNull(ValidationUtils.normalizeEmail(null));
        assertEquals("", ValidationUtils.normalizeEmail("   "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+94771234567",
            "0771234567",
            "771234567",
            "+94711234567",
            "+94701234567",
            "+94761234567",
            "+94781234567",
            "0712345678",
            "0741234567"
    })
    @DisplayName("Valid Sri Lankan mobile phone formats are accepted")
    void testValidSriLankanPhoneFormats(String phone) {
        assertTrue(ValidationUtils.isValidSriLankanPhone(phone), "Expected phone to be valid: " + phone);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+1234567890",      // Non-Sri Lanka country code
            "0112345678",       // Landline number (011)
            "077123456",        // Too short (8 digits)
            "077123456789",     // Too long
            "abcdefghij",       // Alphabetic
            "",                 // Empty
            " "                 // Blank
    })
    @DisplayName("Invalid phone formats are rejected")
    void testInvalidPhoneFormats(String phone) {
        assertFalse(ValidationUtils.isValidSriLankanPhone(phone), "Expected phone to be invalid: " + phone);
    }

    @Test
    @DisplayName("Phone normalization converts local to canonical E.164 international format")
    void testNormalizePhone() {
        assertEquals("+94771234567", ValidationUtils.normalizePhone("0771234567"));
        assertEquals("+94771234567", ValidationUtils.normalizePhone("771234567"));
        assertEquals("+94771234567", ValidationUtils.normalizePhone("+94771234567"));
        assertEquals("+94771234567", ValidationUtils.normalizePhone("94771234567"));
        assertNull(ValidationUtils.normalizePhone(null));
    }
}
