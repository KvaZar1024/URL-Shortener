package com.urlshortener.service;

import com.urlshortener.config.AppConfig;
import com.urlshortener.domain.Link;
import com.urlshortener.repository.LinkRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис для управления короткими ссылками.
 */
public class LinkService {
    private final LinkRepository linkRepository;
    private final ShortCodeGenerator codeGenerator;
    private final NotificationService notificationService;
    private final AppConfig config;

    public LinkService(
            LinkRepository linkRepository,
            ShortCodeGenerator codeGenerator,
            NotificationService notificationService,
            AppConfig config) {
        this.linkRepository = linkRepository;
        this.codeGenerator = codeGenerator;
        this.notificationService = notificationService;
        this.config = config;
    }

    /**
     * Создает новую сокращенную ссылку для пользователя.
     *
     * @param originalUrl URL для сокращения
     * @param userId пользователь, создающий ссылку
     * @return созданная ссылка
     */
    public Link createLink(String originalUrl, UUID userId) {
        return createLink(originalUrl, userId, config.getDefaultClickLimit());
    }

    /**
     * Создает новую сокращенную ссылку с пользовательским лимитом кликов.
     *
     * @param originalUrl URL для сокращения
     * @param userId пользователь, создающий ссылку
     * @param clickLimit пользовательский лимит кликов
     * @return созданная ссылка
     */
    public Link createLink(String originalUrl, UUID userId, int clickLimit) {
        validateUrl(originalUrl);

        String shortCode = generateUniqueShortCode(originalUrl, userId);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(config.getLinkTtlHours());

        Link link =
                Link.builder()
                        .shortCode(shortCode)
                        .originalUrl(originalUrl)
                        .ownerId(userId)
                        .createdAt(now)
                        .expiresAt(expiresAt)
                        .clickLimit(clickLimit)
                        .build();

        linkRepository.save(link);
        return link;
    }

    /**
     * Находит ссылку по ее короткому коду.
     *
     * @param shortCode короткий код
     * @return ссылка, если найдена
     * @throws IllegalArgumentException если ссылка не найдена
     */
    public Link getLink(String shortCode) {
        return linkRepository
                .findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Ссылка не найдена: " + shortCode));
    }

    /**
     * Использует ссылку (увеличивает счетчик кликов и выполняет перенаправление).
     *
     * @param shortCode короткий код
     * @return оригинальный URL, если ссылка активна
     * @throws IllegalStateException если ссылка неактивна или достигнут лимит
     */
    public String useLink(String shortCode) {
        Link link = getLink(shortCode);

        if (link.isExpired()) {
            notificationService.notifyLinkExpired(
                    link.getOwnerId(), shortCode, link.getOriginalUrl());
            throw new IllegalStateException("Срок действия ссылки истек");
        }

        if (!link.use()) {
            if (link.getClickCount() >= link.getClickLimit()) {
                notificationService.notifyLinkLimitReached(
                        link.getOwnerId(), shortCode, link.getOriginalUrl());
                throw new IllegalStateException("Достигнут лимит кликов по ссылке");
            }
            throw new IllegalStateException("Ссылка неактивна");
        }

        linkRepository.save(link);
        return link.getOriginalUrl();
    }

    /**
     * Получает все ссылки, принадлежащие пользователю.
     *
     * @param userId ID пользователя
     * @return список ссылок
     */
    public List<Link> getUserLinks(UUID userId) {
        return linkRepository.findByOwnerId(userId);
    }

    /**
     * Удаляет ссылку, если пользователь является владельцем.
     *
     * @param shortCode короткий код
     * @param userId пользователь, пытающийся удалить
     * @throws IllegalArgumentException если пользователь не является владельцем
     */
    public void deleteLink(String shortCode, UUID userId) {
        Link link = getLink(shortCode);

        if (!link.isOwnedBy(userId)) {
            throw new IllegalArgumentException(
                    "У вас нет прав для удаления этой ссылки");
        }

        linkRepository.deleteByShortCode(shortCode);
    }

    /**
     * Удаляет все истекшие ссылки из репозитория.
     *
     * @return количество удаленных ссылок
     */
    public int cleanupExpiredLinks() {
        List<Link> allLinks = linkRepository.findAll();
        int removedCount = 0;

        for (Link link : allLinks) {
            if (link.isExpired() || !link.isActive()) {
                linkRepository.deleteByShortCode(link.getShortCode());
                removedCount++;
            }
        }

        return removedCount;
    }

    /**
     * Проверяет формат URL.
     */
    private void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL не может быть пустым");
        }

        String normalizedUrl = url.toLowerCase();
        if (!normalizedUrl.startsWith("http://") && !normalizedUrl.startsWith("https://")) {
            throw new IllegalArgumentException(
                    "URL должен начинаться с http:// или https://");
        }

        if (url.length() > 2000) {
            throw new IllegalArgumentException("URL слишком длинный (максимум 2000 символов)");
        }
    }

    /**
     * Генерирует уникальный короткий код, обрабатывая коллизии.
     */
    private String generateUniqueShortCode(String originalUrl, UUID userId) {
        String shortCode = codeGenerator.generate(originalUrl, userId);

        int attempts = 0;
        while (linkRepository.existsByShortCode(shortCode) && attempts < 10) {
            shortCode = codeGenerator.generate(originalUrl + attempts, userId);
            attempts++;
        }

        if (linkRepository.existsByShortCode(shortCode)) {
            throw new IllegalStateException(
                    "Невозможно сгенерировать уникальный короткий код после нескольких попыток");
        }

        return shortCode;
    }
}
