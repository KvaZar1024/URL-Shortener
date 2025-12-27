package com.urlshortener.service;

import com.urlshortener.config.AppConfig;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Фоновый сервис, который периодически очищает истекшие ссылки.
 */
public class CleanupService {
    private final LinkService linkService;
    private final AppConfig config;
    private Timer timer;

    public CleanupService(LinkService linkService, AppConfig config) {
        this.linkService = linkService;
        this.config = config;
    }

    /**
     * Запускает сервис очистки.
     */
    public void start() {
        if (timer != null) {
            return; // Уже запущен
        }

        long intervalMillis = config.getCleanupIntervalMinutes() * 60 * 1000L;

        timer = new Timer("LinkCleanupService", true);
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        performCleanup();
                    }
                },
                intervalMillis,
                intervalMillis);

        System.out.println(
                "Сервис очистки запущен (интервал: "
                        + config.getCleanupIntervalMinutes()
                        + " минут)");
    }

    /**
     * Останавливает сервис очистки.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
            System.out.println("Сервис очистки остановлен");
        }
    }

    /**
     * Выполняет операцию очистки.
     */
    private void performCleanup() {
        try {
            int removed = linkService.cleanupExpiredLinks();
            if (removed > 0) {
                System.out.println(
                        "[Очистка] Удалено " + removed + " истекших/неактивных ссылок");
            }
        } catch (Exception e) {
            System.err.println("Ошибка при очистке: " + e.getMessage());
        }
    }
}
