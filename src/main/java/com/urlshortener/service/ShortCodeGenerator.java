package com.urlshortener.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * Генерирует уникальные короткие коды для URL.
 * Объединяет ID пользователя и оригинальный URL для обеспечения уникальности для каждого пользователя.
 */
public class ShortCodeGenerator {
    private static final String ALPHABET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final int codeLength;

    public ShortCodeGenerator(int codeLength) {
        if (codeLength <= 0) {
            throw new IllegalArgumentException("Длина кода должна быть положительной");
        }
        this.codeLength = codeLength;
    }

    /**
     * Генерирует уникальный короткий код для комбинации URL и пользователя.
     * Разные пользователи получат разные короткие коды для одного и того же URL.
     *
     * @param originalUrl оригинальный URL
     * @param userId ID пользователя
     * @return уникальный короткий код
     */
    public String generate(String originalUrl, UUID userId) {
        try {
            // Объединяем ID пользователя и URL для обеспечения уникальности для каждого пользователя
            String input = userId.toString() + "|" + originalUrl;

            // Хешируем объединенную строку
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            // Преобразуем в Base64 и очищаем
            String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

            // Берем первые N символов и преобразуем в пользовательский алфавит
            return convertToCustomAlphabet(base64.substring(0, Math.min(base64.length(), codeLength * 2)));
        } catch (NoSuchAlgorithmException e) {
            // Запасной вариант: простая случайная генерация
            return generateRandom();
        }
    }

    /**
     * Преобразует строку Base64 для использования пользовательского алфавита для лучшей читаемости.
     */
    private String convertToCustomAlphabet(String base64) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < Math.min(codeLength, base64.length()); i++) {
            char c = base64.charAt(i);
            int index = Math.abs(c) % ALPHABET.length();
            result.append(ALPHABET.charAt(index));
        }
        return result.toString();
    }

    /**
     * Генерирует случайный короткий код в качестве запасного варианта.
     */
    private String generateRandom() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            int index = (int) (Math.random() * ALPHABET.length());
            result.append(ALPHABET.charAt(index));
        }
        return result.toString();
    }
}
