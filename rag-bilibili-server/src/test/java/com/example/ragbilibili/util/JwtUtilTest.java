package com.example.ragbilibili.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(
                "test-secret-key-must-be-at-least-32-bytes-long",
                604800000L
        );
    }

    @Test
    void testGenerateAndParseToken() {
        String token = jwtUtil.generateToken(42L);
        assertThat(token).isNotBlank();
        assertThat(jwtUtil.parseUserId(token)).isEqualTo(42L);
    }

    @Test
    void testIsValid() {
        String token = jwtUtil.generateToken(1L);
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void testIsValidWithInvalidToken() {
        assertThat(jwtUtil.isValid("invalid.token.here")).isFalse();
    }

    @Test
    void testIsValidWithEmptyToken() {
        assertThat(jwtUtil.isValid("")).isFalse();
    }
}
