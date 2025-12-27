package com.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ShortCodeGeneratorTest {

    @Test
    void testGenerateReturnsCorrectLength() {
        ShortCodeGenerator generator = new ShortCodeGenerator(6);
        String code = generator.generate("https://example.com", UUID.randomUUID());

        assertEquals(6, code.length());
    }

    @Test
    void testGenerateUniquenessPerUser() {
        ShortCodeGenerator generator = new ShortCodeGenerator(6);
        String url = "https://example.com";
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        String code1 = generator.generate(url, user1);
        String code2 = generator.generate(url, user2);

        assertNotEquals(code1, code2, "Different users should get different codes for same URL");
    }

    @Test
    void testGenerateDeterministic() {
        ShortCodeGenerator generator = new ShortCodeGenerator(6);
        String url = "https://example.com";
        UUID userId = UUID.randomUUID();

        String code1 = generator.generate(url, userId);
        String code2 = generator.generate(url, userId);

        assertEquals(code1, code2, "Same user and URL should generate same code");
    }

    @Test
    void testGenerateDifferentUrls() {
        ShortCodeGenerator generator = new ShortCodeGenerator(6);
        UUID userId = UUID.randomUUID();

        String code1 = generator.generate("https://example.com", userId);
        String code2 = generator.generate("https://different.com", userId);

        assertNotEquals(code1, code2, "Different URLs should generate different codes");
    }

    @Test
    void testGenerateMultipleCodes() {
        ShortCodeGenerator generator = new ShortCodeGenerator(8);
        UUID userId = UUID.randomUUID();
        Set<String> codes = new HashSet<>();

        for (int i = 0; i < 100; i++) {
            String code = generator.generate("https://example.com/" + i, userId);
            codes.add(code);
            assertEquals(8, code.length());
        }

        assertEquals(100, codes.size(), "All generated codes should be unique");
    }

    @Test
    void testInvalidCodeLength() {
        assertThrows(IllegalArgumentException.class, () -> new ShortCodeGenerator(0));
        assertThrows(IllegalArgumentException.class, () -> new ShortCodeGenerator(-1));
    }
}
