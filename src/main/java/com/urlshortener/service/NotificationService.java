package com.urlshortener.service;

import com.urlshortener.domain.Link;
import java.util.UUID;

/**
 * Сервис для отправки уведомлений пользователям.
 */
public class NotificationService {
    private final boolean enabled;

    public NotificationService(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Уведомляет пользователя о том, что срок действия ссылки истек из-за TTL.
     */
    public void notifyLinkExpired(UUID userId, String shortCode, String originalUrl) {
        if (!enabled) {
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           УВЕДОМЛЕНИЕ: Срок действия ссылки истек         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("  ID пользователя: " + userId);
        System.out.println("  Короткий код: " + shortCode);
        System.out.println("  Оригинальный URL: " + originalUrl);
        System.out.println("  Причина: Истек срок действия (TTL)");
        System.out.println(
                "  Действие: Создайте новую короткую ссылку для продолжения использования этого URL");
        System.out.println();
    }

    /**
     * Уведомляет пользователя о том, что ссылка заблокирована из-за достижения лимита кликов.
     */
    public void notifyLinkLimitReached(UUID userId, String shortCode, String originalUrl) {
        if (!enabled) {
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║         УВЕДОМЛЕНИЕ: Достигнут лимит кликов               ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("  ID пользователя: " + userId);
        System.out.println("  Короткий код: " + shortCode);
        System.out.println("  Оригинальный URL: " + originalUrl);
        System.out.println("  Причина: Достигнуто максимальное количество кликов");
        System.out.println(
                "  Действие: Создайте новую короткую ссылку для продолжения использования этого URL");
        System.out.println();
    }

    /**
     * Уведомляет пользователя о том, что ссылка больше не доступна.
     */
    public void notifyLinkUnavailable(String shortCode, String reason) {
        if (!enabled) {
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            УВЕДОМЛЕНИЕ: Ссылка недоступна                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("  Короткий код: " + shortCode);
        System.out.println("  Причина: " + reason);
        System.out.println();
    }

    /**
     * Отправляет уведомление об успехе.
     */
    public void notifySuccess(String message) {
        if (!enabled) {
            return;
        }

        System.out.println("\n✓ " + message);
    }

    /**
     * Отображает информацию о ссылке.
     */
    public void displayLinkInfo(Link link, String shortDomain) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  Информация о ссылке                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("  Короткий URL: " + shortDomain + "/" + link.getShortCode());
        System.out.println("  Оригинальный URL: " + link.getOriginalUrl());
        System.out.println("  ID владельца: " + link.getOwnerId());
        System.out.println("  Создан: " + link.getCreatedAt());
        System.out.println("  Истекает: " + link.getExpiresAt());
        System.out.println(
                "  Клики: "
                        + link.getClickCount()
                        + "/"
                        + link.getClickLimit()
                        + " (Осталось: "
                        + link.getRemainingClicks()
                        + ")");
        System.out.println("  Статус: " + (link.isActive() ? "Активна" : "Неактивна"));
        System.out.println();
    }
}
