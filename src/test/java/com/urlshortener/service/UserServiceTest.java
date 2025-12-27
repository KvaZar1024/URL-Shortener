package com.urlshortener.service;

import static org.junit.jupiter.api.Assertions.*;

import com.urlshortener.domain.User;
import com.urlshortener.repository.InMemoryUserRepository;
import com.urlshortener.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    void testCreateUser() {
        User user = userService.createUser();

        assertNotNull(user);
        assertNotNull(user.getId());
        assertTrue(userService.userExists(user.getId()));
    }

    @Test
    void testGetUser() {
        User created = userService.createUser();
        User retrieved = userService.getUser(created.getId());

        assertEquals(created.getId(), retrieved.getId());
    }

    @Test
    void testGetUserNotFound() {
        UUID randomId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> userService.getUser(randomId));
    }

    @Test
    void testUserExists() {
        User user = userService.createUser();

        assertTrue(userService.userExists(user.getId()));
        assertFalse(userService.userExists(UUID.randomUUID()));
    }
}
