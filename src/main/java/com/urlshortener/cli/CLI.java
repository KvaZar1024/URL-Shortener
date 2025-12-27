package com.urlshortener.cli;

import com.urlshortener.config.AppConfig;
import com.urlshortener.domain.Link;
import com.urlshortener.domain.User;
import com.urlshortener.service.*;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Интерфейс командной строки для сервиса сокращения URL.
 */
public class CLI {
    private final LinkService linkService;
    private final UserService userService;
    private final BrowserService browserService;
    private final NotificationService notificationService;
    private final CleanupService cleanupService;
    private final AppConfig config;
    private final Scanner scanner;

    private User currentUser;

    public CLI(
            LinkService linkService,
            UserService userService,
            BrowserService browserService,
            NotificationService notificationService,
            CleanupService cleanupService,
            AppConfig config) {
        this.linkService = linkService;
        this.userService = userService;
        this.browserService = browserService;
        this.notificationService = notificationService;
        this.cleanupService = cleanupService;
        this.config = config;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Запускает CLI приложение.
     */
    public void start() {
        printWelcome();
        initializeUser();
        cleanupService.start();

        boolean running = true;
        while (running) {
            try {
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    continue;
                }

                String[] parts = input.split("\\s+", 2);
                String commandStr = parts[0];
                String args = parts.length > 1 ? parts[1] : "";

                Command command = Command.fromString(commandStr);
                if (command == null) {
                    System.out.println("Неизвестная команда: " + commandStr);
                    System.out.println("Введите 'help' для просмотра доступных команд.");
                    continue;
                }

                switch (command) {
                    case CREATE:
                        handleCreate(args);
                        break;
                    case USE:
                        handleUse(args);
                        break;
                    case LIST:
                        handleList();
                        break;
                    case INFO:
                        handleInfo(args);
                        break;
                    case DELETE:
                        handleDelete(args);
                        break;
                    case HELP:
                        printHelp();
                        break;
                    case EXIT:
                        running = false;
                        break;
                }
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }

        shutdown();
    }

    private void printWelcome() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║    Сервис сокращения URL - Консольная версия             ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("\nДобро пожаловать в Сервис сокращения URL!");
        System.out.println("Введите 'help' для просмотра доступных команд.\n");
    }

    private void initializeUser() {
        currentUser = userService.createUser();
        System.out.println("Ваш ID пользователя: " + currentUser.getId());
        System.out.println(
                "Сохраните этот ID, если хотите получить доступ к своим ссылкам в будущих сеансах.");
    }

    private void printHelp() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                  Доступные команды                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        for (Command cmd : Command.values()) {
            System.out.printf("  %-10s - %s%n", cmd.getName(), cmd.getDescription());
        }

        System.out.println("\nИспользование команд:");
        System.out.println("  create <URL> [лимит]  - Создать короткую ссылку (необязательный лимит кликов)");
        System.out.println("  use <короткий_код>    - Открыть оригинальный URL в браузере");
        System.out.println("  info <короткий_код>   - Показать информацию о ссылке");
        System.out.println("  delete <короткий_код> - Удалить ссылку");
        System.out.println("  list                  - Показать список всех ваших ссылок");
        System.out.println();

        System.out.println("Примеры:");
        System.out.println("  create https://example.com");
        System.out.println("  create https://example.com 20");
        System.out.println("  use 3DZHeG");
        System.out.println("  info 3DZHeG");
        System.out.println("  delete 3DZHeG");
    }

    private void handleCreate(String args) {
        if (args.isEmpty()) {
            System.out.println("Использование: create <URL> [лимит_кликов]");
            System.out.println("Пример: create https://example.com 20");
            return;
        }

        String[] parts = args.split("\\s+", 2);
        String url = parts[0];
        int clickLimit = config.getDefaultClickLimit();

        if (parts.length > 1) {
            try {
                clickLimit = Integer.parseInt(parts[1]);
                if (clickLimit <= 0) {
                    System.out.println("Лимит кликов должен быть положительным");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Неверный лимит кликов: " + parts[1]);
                return;
            }
        }

        try {
            Link link = linkService.createLink(url, currentUser.getId(), clickLimit);
            System.out.println("\n✓ Короткая ссылка успешно создана!");
            System.out.println("  Короткий URL: " + config.getShortDomain() + "/" + link.getShortCode());
            System.out.println("  Оригинальный URL: " + link.getOriginalUrl());
            System.out.println("  Лимит кликов: " + link.getClickLimit());
            System.out.println("  Истекает: " + link.getExpiresAt());
        } catch (Exception e) {
            System.err.println("Не удалось создать ссылку: " + e.getMessage());
        }
    }

    private void handleUse(String args) {
        if (args.isEmpty()) {
            System.out.println("Использование: use <короткий_код>");
            System.out.println("Пример: use 3DZHeG");
            return;
        }

        String shortCode = args.trim();

        try {
            String originalUrl = linkService.useLink(shortCode);
            System.out.println("\n✓ Перенаправление на: " + originalUrl);

            browserService.openInBrowser(originalUrl);
        } catch (IllegalStateException e) {
            System.err.println("Ссылка недоступна: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Ссылка не найдена: " + shortCode);
        } catch (IOException e) {
            System.err.println("Не удалось открыть браузер: " + e.getMessage());
        }
    }

    private void handleList() {
        List<Link> links = linkService.getUserLinks(currentUser.getId());

        if (links.isEmpty()) {
            System.out.println("\nУ вас пока нет ссылок.");
            System.out.println("Используйте 'create <URL>' для создания новой короткой ссылки.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                      Ваши ссылки                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();

        for (Link link : links) {
            String status = link.isActive() ? "Активна" : "Неактивна";
            System.out.println("  Короткий код: " + link.getShortCode());
            System.out.println("  Оригинальный URL: " + link.getOriginalUrl());
            System.out.println(
                    "  Клики: "
                            + link.getClickCount()
                            + "/"
                            + link.getClickLimit()
                            + " ("
                            + status
                            + ")");
            System.out.println("  Истекает: " + link.getExpiresAt());
            System.out.println();
        }

        System.out.println("Всего: " + links.size() + " ссылок");
    }

    private void handleInfo(String args) {
        if (args.isEmpty()) {
            System.out.println("Использование: info <короткий_код>");
            System.out.println("Пример: info 3DZHeG");
            return;
        }

        String shortCode = args.trim();

        try {
            Link link = linkService.getLink(shortCode);
            notificationService.displayLinkInfo(link, config.getShortDomain());
        } catch (IllegalArgumentException e) {
            System.err.println("Ссылка не найдена: " + shortCode);
        }
    }

    private void handleDelete(String args) {
        if (args.isEmpty()) {
            System.out.println("Использование: delete <короткий_код>");
            System.out.println("Пример: delete 3DZHeG");
            return;
        }

        String shortCode = args.trim();

        try {
            linkService.deleteLink(shortCode, currentUser.getId());
            System.out.println("\n✓ Ссылка успешно удалена: " + shortCode);
        } catch (IllegalArgumentException e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void shutdown() {
        cleanupService.stop();
        scanner.close();
        System.out.println("\nСпасибо за использование Сервиса сокращения URL!");
        System.out.println("Ваш ID пользователя: " + currentUser.getId());
        System.out.println("До свидания!\n");
    }
}
