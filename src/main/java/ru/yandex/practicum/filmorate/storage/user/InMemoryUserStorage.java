package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @Override
    public User add(User user) {
        user.setId(nextId++);
        if (user.getFriends() == null) {
            user.setFriends(new HashMap<>());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NoSuchElementException("Пользователь с id=" + user.getId() + " не найден");
        }
        // Не затираем существующие friends, если пришло null
        User existing = users.get(user.getId());
        if (user.getFriends() == null) {
            user.setFriends(existing.getFriends());
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    // --- friends methods ---

    @Override
    public void addFriend(int userId, int friendId) {
        User user = getById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId));
        User friend = getById(friendId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + friendId));

        if (user.getFriends() == null) user.setFriends(new HashMap<>());
        if (friend.getFriends() == null) friend.setFriends(new HashMap<>());

        // ставим запрос как UNCONFIRMED
        user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);

        // если уже есть встречный запрос — подтверждаем оба
        if (friend.getFriends().getOrDefault(userId, null) == FriendshipStatus.UNCONFIRMED) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(userId, FriendshipStatus.CONFIRMED);
        } else {
            // если встречного нет — сохраняем как UNCONFIRMED у user
            friend.getFriends().putIfAbsent(userId, friend.getFriends().getOrDefault(userId, null));
        }

        // сохраняем
        users.put(user.getId(), user);
        users.put(friend.getId(), friend);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        User user = getById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId));
        User friend = getById(friendId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + friendId));

        if (user.getFriends() != null) user.getFriends().remove(friendId);

        // если у друга была взаимная связь — пометим её как UNCONFIRMED (или удалим — в зависимости от требований)
        if (friend.getFriends() != null && friend.getFriends().containsKey(userId)) {
            friend.getFriends().put(userId, FriendshipStatus.UNCONFIRMED);
        }

        users.put(user.getId(), user);
        users.put(friend.getId(), friend);
    }

    @Override
    public Collection<User> getFriends(int userId) {
        User user = getById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId));
        if (user.getFriends() == null || user.getFriends().isEmpty()) return Collections.emptyList();
        return user.getFriends().keySet().stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        User user = getById(userId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + userId));
        User other = getById(otherId).orElseThrow(() -> new NoSuchElementException("Пользователь не найден: " + otherId));

        if (user.getFriends() == null || other.getFriends() == null) return Collections.emptyList();

        Set<Integer> common = new HashSet<>(user.getFriends().keySet());
        common.retainAll(other.getFriends().keySet());

        return common.stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // для тестов
    public void clear() {
        users.clear();
        nextId = 1;
    }
}
