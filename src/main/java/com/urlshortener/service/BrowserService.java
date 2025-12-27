package com.urlshortener.service;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Сервис для открытия URL в браузере по умолчанию.
 */
public class BrowserService {

    /**
     * Открывает URL в системном браузере по умолчанию.
     *
     * @param url URL для открытия
     * @throws IllegalStateException если просмотр рабочего стола не поддерживается
     * @throws IOException если возникает ошибка ввода-вывода
     */
    public void openInBrowser(String url) throws IOException {
        if (!Desktop.isDesktopSupported()) {
            throw new IllegalStateException(
                    "Рабочий стол не поддерживается на этой системе. Невозможно открыть браузер.");
        }

        Desktop desktop = Desktop.getDesktop();
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            throw new IllegalStateException(
                    "Действие BROWSE не поддерживается на этой системе. Невозможно открыть браузер.");
        }

        try {
            URI uri = new URI(url);
            desktop.browse(uri);
            System.out.println("Открытие URL в браузере: " + url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Неверный URL: " + url, e);
        }
    }
}
