package com.vutran.expensetracker.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // Nhét một secret key đủ dài (chuẩn HS256 yêu cầu key ít nhất 256-bit) và thời gian sống giả vào
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "ThisIsASecretKeyForTestingPurposeOnlyDoNotUseInProd12345!");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60000); // 60 giây
    }

    @Test
    void testGenerateAndValidateToken_Success() {
        // 1. Tạo token từ email
        String email = "sinhvien@vgu.edu.vn";
        String token = jwtUtils.generateTokenFromEmail(email);

        // 2. Kiểm tra token có được tạo ra không
        assertNotNull(token);
        assertFalse(token.isEmpty());

        // 3. Kiểm tra token có hợp lệ không
        assertTrue(jwtUtils.validateJwtToken(token));

        // 4. Giải mã xem có lấy lại đúng email không
        assertEquals(email, jwtUtils.getEmailFromJwtToken(token));
    }

    @Test
    void testValidateJwtToken_Fail_InvalidToken() {
        String invalidToken = "something_invalid_123456";
        assertFalse(jwtUtils.validateJwtToken(invalidToken));
    }
}