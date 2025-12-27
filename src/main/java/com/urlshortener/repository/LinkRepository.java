package com.urlshortener.repository;

import com.urlshortener.domain.Link;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для операций сохранения Link.
 */
public interface LinkRepository {
    /**
     * Сохраняет ссылку в репозиторий.
     * @param link ссылка для сохранения
     */
    void save(Link link);

    /**
     * Находит ссылку по короткому коду.
     * @param shortCode короткий код для поиска
     * @return Optional, содержащий ссылку, если найдена
     */
    Optional<Link> findByShortCode(String shortCode);

    /**
     * Находит все ссылки, принадлежащие определенному пользователю.
     * @param userId ID пользователя
     * @return список ссылок, принадлежащих пользователю
     */
    List<Link> findByOwnerId(UUID userId);

    /**
     * Находит все ссылки в репозитории.
     * @return список всех ссылок
     */
    List<Link> findAll();

    /**
     * Удаляет ссылку по короткому коду.
     * @param shortCode короткий код ссылки для удаления
     * @return true, если ссылка была удалена, false, если она не существовала
     */
    boolean deleteByShortCode(String shortCode);

    /**
     * Проверяет, существует ли короткий код.
     * @param shortCode короткий код для проверки
     * @return true, если короткий код существует
     */
    boolean existsByShortCode(String shortCode);
}
