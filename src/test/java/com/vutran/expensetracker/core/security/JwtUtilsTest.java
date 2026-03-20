package com.vutran.expensetracker.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtUtils to verify token generation, validation, and payload extraction.
 */
public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        
        // Inject mock configurations into private fields using ReflectionTestUtils.
        // The secret key must meet the HS256 requirement (minimum 256-bit length).
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "ThisIsASecretKeyForTestingPurposeOnlyDoNotUseInProd12345!");
        
        // Set a short expiration period (60 seconds) for the testing environment.
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000); 
    }

    /**
     * Verifies the successful lifecycle of a JWT, including generation, 
     * integrity validation, and accurate email extraction.
     */
    @Test
    void testGenerateAndValidateToken_Success() {
        // 1. Generate a token from a provided email address
        String email = "sinhvien@vgu.edu.vn";
        String token = jwtUtils.generateTokenFromEmail(email);

        // 2. Assert that the generated token is not null or empty
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 3. Validate the cryptographic integrity of the token
        assertTrue(jwtUtils.validateJwtToken(token));

        // 4. Decrypt the token to ensure the extracted email matches the original input
        assertEquals(email, jwtUtils.getEmailFromJwtToken(token));
    }

    /**
     * Verifies that the system correctly rejects malformed or invalid tokens.
     */
    @Test
    void testValidateJwtToken_Fail_InvalidToken() {
        String invalidToken = "something_invalid_123456";
        
        // Ensure the validator returns false for unauthorized token structures
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }
}