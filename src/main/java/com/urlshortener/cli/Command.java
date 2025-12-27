package com.urlshortener.cli;

/**
 * Перечисление, представляющее доступные команды CLI.
 */
public enum Command {
    CREATE("create", "Создать новую короткую ссылку"),
    USE("use", "Использовать короткую ссылку (перенаправление на оригинальный URL)"),
    LIST("list", "Показать список всех ваших ссылок"),
    INFO("info", "Показать информацию о конкретной ссылке"),
    DELETE("delete", "Удалить ссылку"),
    HELP("help", "Показать справочное сообщение"),
    EXIT("exit", "Выйти из приложения");

    private final String name;
    private final String description;

    Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static Command fromString(String text) {
        for (Command cmd : Command.values()) {
            if (cmd.name.equalsIgnoreCase(text)) {
                return cmd;
            }
        }
        return null;
    }
}
