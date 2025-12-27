package com.urlshortener.domain;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Представляет сокращенную ссылку с метаданными и контролем доступа.
 */
public class Link {
    private final String shortCode;
    private final String originalUrl;
    private final UUID ownerId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private final int clickLimit;
    private int clickCount;
    private boolean active;

    private Link(Builder builder) {
        this.shortCode = builder.shortCode;
        this.originalUrl = builder.originalUrl;
        this.ownerId = builder.ownerId;
        this.createdAt = builder.createdAt;
        this.expiresAt = builder.expiresAt;
        this.clickLimit = builder.clickLimit;
        this.clickCount = 0;
        this.active = true;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Попытка использовать ссылку (увеличить счетчик переходов).
     * @return true если ссылка успешно использована, false если она неактивна или достигнут лимит
     */
    public boolean use() {
        if (!active) {
            return false;
        }

        if (isExpired()) {
            active = false;
            return false;
        }

        if (clickCount >= clickLimit) {
            active = false;
            return false;
        }

        clickCount++;

        if (clickCount >= clickLimit) {
            active = false;
        }

        return true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }

    public int getRemainingClicks() {
        return Math.max(0, clickLimit - clickCount);
    }

    public void updateClickLimit(int newLimit) {
        if (newLimit < clickCount) {
            throw new IllegalArgumentException(
                "Новый лимит не может быть меньше текущего количества переходов");
        }
        // Это потребует сделать clickLimit не-final или использовать другой подход
        // Для простоты, мы создадим новый Link на уровне сервиса
        throw new UnsupportedOperationException(
            "Обновление лимита переходов требует создания нового экземпляра Link");
    }

    // Getters
    public String getShortCode() {
        return shortCode;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public int getClickLimit() {
        return clickLimit;
    }

    public int getClickCount() {
        return clickCount;
    }

    public boolean isActive() {
        return active && !isExpired();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(shortCode, link.shortCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shortCode);
    }

    @Override
    public String toString() {
        return "Link{"
                + "shortCode='"
                + shortCode
                + '\''
                + ", originalUrl='"
                + originalUrl
                + '\''
                + ", ownerId="
                + ownerId
                + ", createdAt="
                + createdAt
                + ", expiresAt="
                + expiresAt
                + ", clickLimit="
                + clickLimit
                + ", clickCount="
                + clickCount
                + ", active="
                + active
                + '}';
    }

    public static class Builder {
        private String shortCode;
        private String originalUrl;
        private UUID ownerId;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private int clickLimit;

        public Builder shortCode(String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public Builder originalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
            return this;
        }

        public Builder ownerId(UUID ownerId) {
            this.ownerId = ownerId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder clickLimit(int clickLimit) {
            this.clickLimit = clickLimit;
            return this;
        }

        public Link build() {
            Objects.requireNonNull(shortCode, "Короткий код не может быть null");
            Objects.requireNonNull(originalUrl, "Оригинальный URL не может быть null");
            Objects.requireNonNull(ownerId, "ID владельца не может быть null");
            Objects.requireNonNull(createdAt, "Дата создания не может быть null");
            Objects.requireNonNull(expiresAt, "Дата истечения не может быть null");

            if (clickLimit <= 0) {
                throw new IllegalArgumentException("Лимит переходов должен быть положительным");
            }

            if (expiresAt.isBefore(createdAt)) {
                throw new IllegalArgumentException("Дата истечения должна быть после даты создания");
            }

            return new Link(this);
        }
    }
}
