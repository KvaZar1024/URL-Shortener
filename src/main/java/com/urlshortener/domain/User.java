package com.urlshortener.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Представляет пользователя в системе сокращения URL.
 * Каждый пользователь идентифицируется уникальным UUID.
 */
public class User {
    private final UUID id;

    public User(UUID id) {
        this.id = Objects.requireNonNull(id, "ID пользователя не может быть null");
    }

    public static User create() {
        return new User(UUID.randomUUID());
    }

    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=" + id + '}';
    }
}
