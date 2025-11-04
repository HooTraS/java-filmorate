package ru.yandex.practicum.filmorate.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @Autowired
    public UserDbStorageTest(UserDbStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Test
    void testAddAndGetById() {
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("login1");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User savedUser = userStorage.add(user);
        Optional<User> loadedUser = userStorage.getById(savedUser.getId());

        assertThat(loadedUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("email", "user@mail.ru")
                                .hasFieldOrPropertyWithValue("login", "login1")
                                .hasFieldOrPropertyWithValue("name", "Test User")
                );
    }

    @Test
    void testUpdateUser() {
        User user = new User();
        user.setEmail("old@mail.ru");
        user.setLogin("oldLogin");
        user.setName("Old Name");
        user.setBirthday(LocalDate.of(1985, 5, 5));

        User savedUser = userStorage.add(user);

        savedUser.setName("New Name");
        savedUser.setEmail("new@mail.ru");
        userStorage.update(savedUser);

        Optional<User> updatedUser = userStorage.getById(savedUser.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("name", "New Name")
                                .hasFieldOrPropertyWithValue("email", "new@mail.ru")
                );
    }
}
