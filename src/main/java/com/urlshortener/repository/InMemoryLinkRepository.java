package com.urlshortener.repository;

import com.urlshortener.domain.Link;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory реализация LinkRepository с использованием ConcurrentHashMap для потокобезопасности.
 */
public class InMemoryLinkRepository implements LinkRepository {
    private final Map<String, Link> links = new ConcurrentHashMap<>();

    @Override
    public void save(Link link) {
        links.put(link.getShortCode(), link);
    }

    @Override
    public Optional<Link> findByShortCode(String shortCode) {
        return Optional.ofNullable(links.get(shortCode));
    }

    @Override
    public List<Link> findByOwnerId(UUID userId) {
        return links.values().stream()
                .filter(link -> link.getOwnerId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Link> findAll() {
        return new ArrayList<>(links.values());
    }

    @Override
    public boolean deleteByShortCode(String shortCode) {
        return links.remove(shortCode) != null;
    }

    @Override
    public boolean existsByShortCode(String shortCode) {
        return links.containsKey(shortCode);
    }
}
