package com.urlshortener.service;

import com.urlshortener.domain.User;
import com.urlshortener.repository.UserRepository;
import java.util.UUID;

/**
 * Сервис для управления пользователями.
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Создает нового пользователя с сгенерированным UUID.
     * @return созданный пользователь
     */
    public User createUser() {
        User user = User.create();
        userRepository.save(user);
        return user;
    }

    /**
     * Находит пользователя по ID.
     * @param userId ID пользователя
     * @return пользователь, если найден
     * @throws IllegalArgumentException если пользователь не найден
     */
    public User getUser(UUID userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден: " + userId));
    }

    /**
     * Проверяет, существует ли пользователь.
     * @param userId ID пользователя
     * @return true, если пользователь существует
     */
    public boolean userExists(UUID userId) {
        return userRepository.existsById(userId);
    }
}
