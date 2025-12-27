package com.urlshortener.repository;

import com.urlshortener.domain.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс репозитория для операций сохранения User.
 */
public interface UserRepository {
    /**
     * Сохраняет пользователя в репозиторий.
     * @param user пользователь для сохранения
     */
    void save(User user);

    /**
     * Находит пользователя по ID.
     * @param id ID пользователя
     * @return Optional, содержащий пользователя, если найден
     */
    Optional<User> findById(UUID id);

    /**
     * Проверяет, существует ли пользователь по ID.
     * @param id ID пользователя
     * @return true, если пользователь существует
     */
    boolean existsById(UUID id);
}
