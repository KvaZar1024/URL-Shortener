package com.urlshortener.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void testUserCreation() {
        UUID id = UUID.randomUUID();
        User user = new User(id);

        assertEquals(id, user.getId());
    }

    @Test
    void testUserCreate() {
        User user = User.create();

        assertNotNull(user.getId());
    }

    @Test
    void testUserEquality() {
        UUID id = UUID.randomUUID();
        User user1 = new User(id);
        User user2 = new User(id);
        User user3 = new User(UUID.randomUUID());

        assertEquals(user1, user2);
        assertNotEquals(user1, user3);
    }

    @Test
    void testNullId() {
        assertThrows(NullPointerException.class, () -> new User(null));
    }
}
