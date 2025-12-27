package com.urlshortener.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Конфигурация приложения, загружаемая из файла свойств.
 */
public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";

    private final int linkTtlHours;
    private final int defaultClickLimit;
    private final int shortCodeLength;
    private final String shortDomain;
    private final int cleanupIntervalMinutes;
    private final boolean notificationsEnabled;

    private AppConfig(Properties properties) {
        this.linkTtlHours = getIntProperty(properties, "link.ttl.hours", 24);
        this.defaultClickLimit = getIntProperty(properties, "link.default.click.limit", 10);
        this.shortCodeLength = getIntProperty(properties, "link.short.code.length", 6);
        this.shortDomain = properties.getProperty("link.short.domain", "clck.ru");
        this.cleanupIntervalMinutes =
                getIntProperty(properties, "cleanup.interval.minutes", 5);
        this.notificationsEnabled =
                Boolean.parseBoolean(
                        properties.getProperty("notifications.enabled", "true"));
    }

    /**
     * Загружает конфигурацию из файла свойств по умолчанию.
     * @return экземпляр AppConfig
     */
    public static AppConfig load() {
        Properties properties = new Properties();
        try (InputStream input =
                AppConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                System.out.println(
                        "Предупреждение: " + CONFIG_FILE + " не найден, используются значения по умолчанию");
                return new AppConfig(properties);
            }
            properties.load(input);
        } catch (IOException e) {
            System.err.println("Ошибка загрузки конфигурации: " + e.getMessage());
        }
        return new AppConfig(properties);
    }

    private static int getIntProperty(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            System.err.println(
                    "Неверное значение для "
                            + key
                            + ": "
                            + value
                            + ", используется значение по умолчанию: "
                            + defaultValue);
            return defaultValue;
        }
    }

    public int getLinkTtlHours() {
        return linkTtlHours;
    }

    public int getDefaultClickLimit() {
        return defaultClickLimit;
    }

    public int getShortCodeLength() {
        return shortCodeLength;
    }

    public String getShortDomain() {
        return shortDomain;
    }

    public int getCleanupIntervalMinutes() {
        return cleanupIntervalMinutes;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    @Override
    public String toString() {
        return "AppConfig{"
                + "linkTtlHours="
                + linkTtlHours
                + ", defaultClickLimit="
                + defaultClickLimit
                + ", shortCodeLength="
                + shortCodeLength
                + ", shortDomain='"
                + shortDomain
                + '\''
                + ", cleanupIntervalMinutes="
                + cleanupIntervalMinutes
                + ", notificationsEnabled="
                + notificationsEnabled
                + '}';
    }
}
