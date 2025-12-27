package com.urlshortener.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LinkTest {

    @Test
    void testLinkCreation() {
        UUID ownerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        Link link =
                Link.builder()
                        .shortCode("abc123")
                        .originalUrl("https://example.com")
                        .ownerId(ownerId)
                        .createdAt(now)
                        .expiresAt(expiresAt)
                        .clickLimit(10)
                        .build();

        assertEquals("abc123", link.getShortCode());
        assertEquals("https://example.com", link.getOriginalUrl());
        assertEquals(ownerId, link.getOwnerId());
        assertEquals(10, link.getClickLimit());
        assertEquals(0, link.getClickCount());
        assertTrue(link.isActive());
    }

    @Test
    void testLinkUse() {
        UUID ownerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        Link link =
                Link.builder()
                        .shortCode("abc123")
                        .originalUrl("https://example.com")
                        .ownerId(ownerId)
                        .createdAt(now)
                        .expiresAt(expiresAt)
                        .clickLimit(3)
                        .build();

        assertTrue(link.use());
        assertEquals(1, link.getClickCount());
        assertTrue(link.isActive());

        assertTrue(link.use());
        assertEquals(2, link.getClickCount());
        assertTrue(link.isActive());

        assertTrue(link.use());
        assertEquals(3, link.getClickCount());
        assertFalse(link.isActive());

        assertFalse(link.use());
        assertEquals(3, link.getClickCount());
    }

    @Test
    void testLinkExpiration() {
        UUID ownerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.minusHours(1); // Already expired

        Link link =
                Link.builder()
                        .shortCode("abc123")
                        .originalUrl("https://example.com")
                        .ownerId(ownerId)
                        .createdAt(now.minusHours(2))
                        .expiresAt(expiresAt)
                        .clickLimit(10)
                        .build();

        assertTrue(link.isExpired());
        assertFalse(link.isActive());
        assertFalse(link.use());
    }

    @Test
    void testRemainingClicks() {
        UUID ownerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        Link link =
                Link.builder()
                        .shortCode("abc123")
                        .originalUrl("https://example.com")
                        .ownerId(ownerId)
                        .createdAt(now)
                        .expiresAt(expiresAt)
                        .clickLimit(5)
                        .build();

        assertEquals(5, link.getRemainingClicks());

        link.use();
        assertEquals(4, link.getRemainingClicks());

        link.use();
        link.use();
        assertEquals(2, link.getRemainingClicks());
    }

    @Test
    void testOwnership() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Link link =
                Link.builder()
                        .shortCode("abc123")
                        .originalUrl("https://example.com")
                        .ownerId(ownerId)
                        .createdAt(now)
                        .expiresAt(now.plusHours(24))
                        .clickLimit(10)
                        .build();

        assertTrue(link.isOwnedBy(ownerId));
        assertFalse(link.isOwnedBy(otherUserId));
    }

    @Test
    void testInvalidLinkCreation() {
        UUID ownerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        assertThrows(
                NullPointerException.class,
                () -> {
                    Link.builder()
                            .originalUrl("https://example.com")
                            .ownerId(ownerId)
                            .createdAt(now)
                            .expiresAt(now.plusHours(24))
                            .clickLimit(10)
                            .build();
                });

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Link.builder()
                            .shortCode("abc123")
                            .originalUrl("https://example.com")
                            .ownerId(ownerId)
                            .createdAt(now)
                            .expiresAt(now.plusHours(24))
                            .clickLimit(0)
                            .build();
                });

        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    Link.builder()
                            .shortCode("abc123")
                            .originalUrl("https://example.com")
                            .ownerId(ownerId)
                            .createdAt(now)
                            .expiresAt(now.minusHours(1))
                            .clickLimit(10)
                            .build();
                });
    }
}
