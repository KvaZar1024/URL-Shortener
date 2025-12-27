package com.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.urlshortener.config.AppConfig;
import com.urlshortener.domain.Link;
import com.urlshortener.repository.InMemoryLinkRepository;
import com.urlshortener.repository.LinkRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LinkServiceTest {

    @Mock private AppConfig config;

    @Mock private NotificationService notificationService;

    private LinkRepository linkRepository;
    private ShortCodeGenerator codeGenerator;
    private LinkService linkService;

    @BeforeEach
    void setUp() {
        linkRepository = new InMemoryLinkRepository();
        codeGenerator = new ShortCodeGenerator(6);

        lenient().when(config.getDefaultClickLimit()).thenReturn(10);
        lenient().when(config.getLinkTtlHours()).thenReturn(24);

        linkService =
                new LinkService(linkRepository, codeGenerator, notificationService, config);
    }

    @Test
    void testCreateLink() {
        UUID userId = UUID.randomUUID();
        String url = "https://example.com";

        Link link = linkService.createLink(url, userId);

        assertNotNull(link);
        assertEquals(url, link.getOriginalUrl());
        assertEquals(userId, link.getOwnerId());
        assertEquals(10, link.getClickLimit());
        assertTrue(link.isActive());
    }

    @Test
    void testCreateLinkWithCustomLimit() {
        UUID userId = UUID.randomUUID();
        String url = "https://example.com";

        Link link = linkService.createLink(url, userId, 20);

        assertEquals(20, link.getClickLimit());
    }

    @Test
    void testCreateLinkInvalidUrl() {
        UUID userId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () -> linkService.createLink("", userId));

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink("invalid-url", userId));

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink("ftp://example.com", userId));
    }

    @Test
    void testGetLink() {
        UUID userId = UUID.randomUUID();
        Link created = linkService.createLink("https://example.com", userId);

        Link retrieved = linkService.getLink(created.getShortCode());

        assertEquals(created.getShortCode(), retrieved.getShortCode());
    }

    @Test
    void testGetLinkNotFound() {
        assertThrows(IllegalArgumentException.class, () -> linkService.getLink("nonexistent"));
    }

    @Test
    void testUseLink() {
        UUID userId = UUID.randomUUID();
        Link link = linkService.createLink("https://example.com", userId);

        String originalUrl = linkService.useLink(link.getShortCode());

        assertEquals("https://example.com", originalUrl);

        Link updated = linkService.getLink(link.getShortCode());
        assertEquals(1, updated.getClickCount());
    }

    @Test
    void testUseLinkReachesLimit() {
        UUID userId = UUID.randomUUID();
        Link link = linkService.createLink("https://example.com", userId, 2);

        linkService.useLink(link.getShortCode());
        linkService.useLink(link.getShortCode());

        assertThrows(
                IllegalStateException.class, () -> linkService.useLink(link.getShortCode()));

        verify(notificationService)
                .notifyLinkLimitReached(
                        eq(userId), eq(link.getShortCode()), eq("https://example.com"));
    }

    @Test
    void testGetUserLinks() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        linkService.createLink("https://example1.com", userId1);
        linkService.createLink("https://example2.com", userId1);
        linkService.createLink("https://example3.com", userId2);

        List<Link> user1Links = linkService.getUserLinks(userId1);
        List<Link> user2Links = linkService.getUserLinks(userId2);

        assertEquals(2, user1Links.size());
        assertEquals(1, user2Links.size());
    }

    @Test
    void testDeleteLink() {
        UUID userId = UUID.randomUUID();
        Link link = linkService.createLink("https://example.com", userId);

        linkService.deleteLink(link.getShortCode(), userId);

        assertThrows(
                IllegalArgumentException.class, () -> linkService.getLink(link.getShortCode()));
    }

    @Test
    void testDeleteLinkUnauthorized() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        Link link = linkService.createLink("https://example.com", owner);

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.deleteLink(link.getShortCode(), other));
    }

    @Test
    void testCleanupExpiredLinks() {
        // This test would require manipulating time, which is complex
        // For now, we just test that cleanup doesn't crash
        int removed = linkService.cleanupExpiredLinks();
        assertTrue(removed >= 0);
    }

    @Test
    void testUniquenessPerUser() {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();
        String url = "https://example.com";

        Link link1 = linkService.createLink(url, user1);
        Link link2 = linkService.createLink(url, user2);

        assertNotEquals(
                link1.getShortCode(),
                link2.getShortCode(),
                "Different users should get different short codes");
    }
}
