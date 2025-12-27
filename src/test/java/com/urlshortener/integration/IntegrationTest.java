package com.urlshortener.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.urlshortener.config.AppConfig;
import com.urlshortener.domain.Link;
import com.urlshortener.domain.User;
import com.urlshortener.repository.InMemoryLinkRepository;
import com.urlshortener.repository.InMemoryUserRepository;
import com.urlshortener.service.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the entire URL shortener workflow.
 */
class IntegrationTest {

    private LinkService linkService;
    private UserService userService;
    private AppConfig config;

    @BeforeEach
    void setUp() {
        config = AppConfig.load();
        InMemoryLinkRepository linkRepository = new InMemoryLinkRepository();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        ShortCodeGenerator codeGenerator = new ShortCodeGenerator(config.getShortCodeLength());
        NotificationService notificationService =
                new NotificationService(false); // Disable for tests

        userService = new UserService(userRepository);
        linkService =
                new LinkService(linkRepository, codeGenerator, notificationService, config);
    }

    @Test
    void testCompleteWorkflow() {
        // Create user
        User user = userService.createUser();
        assertNotNull(user.getId());

        // Create link
        String originalUrl = "https://www.example.com/very/long/path";
        Link link = linkService.createLink(originalUrl, user.getId());

        assertNotNull(link.getShortCode());
        assertEquals(originalUrl, link.getOriginalUrl());
        assertEquals(user.getId(), link.getOwnerId());
        assertEquals(0, link.getClickCount());
        assertTrue(link.isActive());

        // Use link multiple times
        for (int i = 0; i < 3; i++) {
            String url = linkService.useLink(link.getShortCode());
            assertEquals(originalUrl, url);
        }

        // Check click count
        Link updatedLink = linkService.getLink(link.getShortCode());
        assertEquals(3, updatedLink.getClickCount());
        assertTrue(updatedLink.isActive());

        // List user links
        List<Link> userLinks = linkService.getUserLinks(user.getId());
        assertEquals(1, userLinks.size());
        assertEquals(link.getShortCode(), userLinks.get(0).getShortCode());

        // Delete link
        linkService.deleteLink(link.getShortCode(), user.getId());
        assertThrows(
                IllegalArgumentException.class, () -> linkService.getLink(link.getShortCode()));
    }

    @Test
    void testMultipleUsersUniqueness() {
        User user1 = userService.createUser();
        User user2 = userService.createUser();

        String url = "https://www.example.com";

        Link link1 = linkService.createLink(url, user1.getId());
        Link link2 = linkService.createLink(url, user2.getId());

        assertNotEquals(
                link1.getShortCode(),
                link2.getShortCode(),
                "Different users should get different short codes for same URL");
        assertEquals(url, link1.getOriginalUrl());
        assertEquals(url, link2.getOriginalUrl());
    }

    @Test
    void testClickLimitExhaustion() {
        User user = userService.createUser();
        Link link = linkService.createLink("https://example.com", user.getId(), 3);

        // Use link 3 times (limit)
        linkService.useLink(link.getShortCode());
        linkService.useLink(link.getShortCode());
        linkService.useLink(link.getShortCode());

        // 4th attempt should fail
        assertThrows(
                IllegalStateException.class, () -> linkService.useLink(link.getShortCode()));

        Link exhaustedLink = linkService.getLink(link.getShortCode());
        assertEquals(3, exhaustedLink.getClickCount());
        assertFalse(exhaustedLink.isActive());
    }

    @Test
    void testMultipleLinksPerUser() {
        User user = userService.createUser();

        Link link1 = linkService.createLink("https://example1.com", user.getId());
        Link link2 = linkService.createLink("https://example2.com", user.getId());
        Link link3 = linkService.createLink("https://example3.com", user.getId());

        List<Link> userLinks = linkService.getUserLinks(user.getId());
        assertEquals(3, userLinks.size());

        // All links should have different short codes
        assertNotEquals(link1.getShortCode(), link2.getShortCode());
        assertNotEquals(link1.getShortCode(), link3.getShortCode());
        assertNotEquals(link2.getShortCode(), link3.getShortCode());
    }

    @Test
    void testUnauthorizedDeletion() {
        User owner = userService.createUser();
        User other = userService.createUser();

        Link link = linkService.createLink("https://example.com", owner.getId());

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.deleteLink(link.getShortCode(), other.getId()));

        // Link should still exist
        assertDoesNotThrow(() -> linkService.getLink(link.getShortCode()));
    }

    @Test
    void testInvalidUrlHandling() {
        User user = userService.createUser();

        assertThrows(
                IllegalArgumentException.class, () -> linkService.createLink("", user.getId()));

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink("not-a-url", user.getId()));

        assertThrows(
                IllegalArgumentException.class,
                () -> linkService.createLink("ftp://example.com", user.getId()));
    }

    @Test
    void testCleanupExpiredLinks() {
        User user = userService.createUser();

        // Create some links
        linkService.createLink("https://example1.com", user.getId());
        linkService.createLink("https://example2.com", user.getId());

        // Cleanup (should not remove active links)
        int removed = linkService.cleanupExpiredLinks();

        // Since links are not expired yet, nothing should be removed
        assertEquals(0, removed);

        List<Link> userLinks = linkService.getUserLinks(user.getId());
        assertEquals(2, userLinks.size());
    }
}
