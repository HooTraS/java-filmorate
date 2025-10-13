package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User add(User user) {
        validate(user);
        return userStorage.add(user);
    }

    public User update(User user) {
        validate(user);
        userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + user.getId() + " не найден"));
        return userStorage.update(user);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public User getById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        getById(userId);
        getById(friendId);
        Optional.ofNullable(friends.get(userId)).ifPresent(s -> s.remove(friendId));
        Optional.ofNullable(friends.get(friendId)).ifPresent(s -> s.remove(userId));
    }

    public Collection<User> getFriends(int userId) {
        getById(userId);
        return friends.getOrDefault(userId, Set.of()).stream()
                .map(this::getById)
                .toList();
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        getById(userId);
        getById(otherId);
        Set<Integer> set1 = new HashSet<>(friends.getOrDefault(userId, Set.of()));
        Set<Integer> set2 = new HashSet<>(friends.getOrDefault(otherId, Set.of()));
        set1.retainAll(set2);
        return set1.stream().map(this::getById).toList();
    }

    private void validate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым или содержать пробелы");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(java.time.LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
