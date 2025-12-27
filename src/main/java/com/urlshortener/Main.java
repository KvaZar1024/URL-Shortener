package com.urlshortener;

import com.urlshortener.cli.CLI;
import com.urlshortener.config.AppConfig;
import com.urlshortener.repository.InMemoryLinkRepository;
import com.urlshortener.repository.InMemoryUserRepository;
import com.urlshortener.repository.LinkRepository;
import com.urlshortener.repository.UserRepository;
import com.urlshortener.service.*;

/**
 * Главная точка входа для приложения сервиса сокращения URL.
 */
public class Main {
    public static void main(String[] args) {
        // Загрузка конфигурации
        AppConfig config = AppConfig.load();

        // Инициализация репозиториев
        LinkRepository linkRepository = new InMemoryLinkRepository();
        UserRepository userRepository = new InMemoryUserRepository();

        // Инициализация сервисов
        ShortCodeGenerator codeGenerator = new ShortCodeGenerator(config.getShortCodeLength());
        NotificationService notificationService =
                new NotificationService(config.isNotificationsEnabled());
        UserService userService = new UserService(userRepository);
        LinkService linkService =
                new LinkService(linkRepository, codeGenerator, notificationService, config);
        BrowserService browserService = new BrowserService();
        CleanupService cleanupService = new CleanupService(linkService, config);

        // Инициализация и запуск CLI
        CLI cli =
                new CLI(
                        linkService,
                        userService,
                        browserService,
                        notificationService,
                        cleanupService,
                        config);

        cli.start();
    }
}
