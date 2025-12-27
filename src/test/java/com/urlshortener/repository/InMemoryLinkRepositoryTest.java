package com.urlshortener.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.urlshortener.domain.Link;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryLinkRepositoryTest {

    private LinkRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLinkRepository();
    }

    private Link createTestLink(String shortCode, UUID ownerId) {
        LocalDateTime now = LocalDateTime.now();
        return Link.builder()
                .shortCode(shortCode)
                .originalUrl("https://example.com")
                .ownerId(ownerId)
                .createdAt(now)
                .expiresAt(now.plusHours(24))
                .clickLimit(10)
                .build();
    }

    @Test
    void testSaveAndFindByShortCode() {
        UUID ownerId = UUID.randomUUID();
        Link link = createTestLink("abc123", ownerId);

        repository.save(link);

        Optional<Link> found = repository.findByShortCode("abc123");
        assertTrue(found.isPresent());
        assertEquals("abc123", found.get().getShortCode());
    }

    @Test
    void testFindByShortCodeNotFound() {
        Optional<Link> found = repository.findByShortCode("nonexistent");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByOwnerId() {
        UUID owner1 = UUID.randomUUID();
        UUID owner2 = UUID.randomUUID();

        repository.save(createTestLink("abc1", owner1));
        repository.save(createTestLink("abc2", owner1));
        repository.save(createTestLink("abc3", owner2));

        List<Link> owner1Links = repository.findByOwnerId(owner1);
        List<Link> owner2Links = repository.findByOwnerId(owner2);

        assertEquals(2, owner1Links.size());
        assertEquals(1, owner2Links.size());
    }

    @Test
    void testFindAll() {
        UUID ownerId = UUID.randomUUID();

        repository.save(createTestLink("abc1", ownerId));
        repository.save(createTestLink("abc2", ownerId));
        repository.save(createTestLink("abc3", ownerId));

        List<Link> allLinks = repository.findAll();
        assertEquals(3, allLinks.size());
    }

    @Test
    void testDeleteByShortCode() {
        UUID ownerId = UUID.randomUUID();
        Link link = createTestLink("abc123", ownerId);

        repository.save(link);
        assertTrue(repository.existsByShortCode("abc123"));

        boolean deleted = repository.deleteByShortCode("abc123");
        assertTrue(deleted);
        assertFalse(repository.existsByShortCode("abc123"));
    }

    @Test
    void testDeleteByShortCodeNotFound() {
        boolean deleted = repository.deleteByShortCode("nonexistent");
        assertFalse(deleted);
    }

    @Test
    void testExistsByShortCode() {
        UUID ownerId = UUID.randomUUID();
        Link link = createTestLink("abc123", ownerId);

        assertFalse(repository.existsByShortCode("abc123"));

        repository.save(link);
        assertTrue(repository.existsByShortCode("abc123"));
    }

    @Test
    void testUpdateLink() {
        UUID ownerId = UUID.randomUUID();
        Link link = createTestLink("abc123", ownerId);

        repository.save(link);

        // Use the link to increment click count
        link.use();

        // Save again (update)
        repository.save(link);

        Optional<Link> updated = repository.findByShortCode("abc123");
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().getClickCount());
    }
}
