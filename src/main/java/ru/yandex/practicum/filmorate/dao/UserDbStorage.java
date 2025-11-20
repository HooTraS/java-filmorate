package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps =
                    connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        Optional<User> user = jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findFirst();

        user.ifPresent(u -> u.setFriends(loadFriends(id)));

        return user;
    }

    private Map<Integer, FriendshipStatus> loadFriends(int userId) {
        String sql = "SELECT friend_id, status FROM friends WHERE user_id = ?";

        List<Map.Entry<Integer, FriendshipStatus>> list = jdbcTemplate.query(sql,
                (rs, row) -> Map.entry(
                        rs.getInt("friend_id"),
                        rs.getBoolean("status")
                                ? FriendshipStatus.CONFIRMED
                                : FriendshipStatus.UNCONFIRMED
                ),
                userId);

        Map<Integer, FriendshipStatus> map = new HashMap<>();
        list.forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;
    }

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        Boolean reverse = null;

        try {
            reverse = jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                    Boolean.class,
                    friendId,
                    userId
            );
        } catch (Exception ignored) {
        }

        if (reverse == null) {
            jdbcTemplate.update(
                    "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)",
                    userId, friendId, false
            );
        } else if (!reverse) {
            jdbcTemplate.update("UPDATE friends SET status = TRUE WHERE user_id = ? AND friend_id = ?",
                    friendId, userId);
            jdbcTemplate.update(
                    "MERGE INTO friends(user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, TRUE)",
                    userId, friendId
            );
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friends WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        Boolean reverse = null;
        try {
            reverse = jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                    Boolean.class,
                    friendId,
                    userId
            );
        } catch (Exception ignored) {
        }

        if (reverse != null && reverse) {
            jdbcTemplate.update(
                    "UPDATE friends SET status = FALSE WHERE user_id = ? AND friend_id = ?",
                    friendId, userId
            );
        }
    }

    @Override
    public Collection<User> getFriends(int userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";

        List<Integer> ids = jdbcTemplate.query(sql,
                (rs, row) -> rs.getInt("friend_id"),
                userId);

        return ids.stream()
                .map(this::getById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        String sql = """
                SELECT f1.friend_id
                FROM friends f1
                JOIN friends f2 ON f1.friend_id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;

        List<Integer> ids = jdbcTemplate.query(sql,
                (rs, row) -> rs.getInt("friend_id"),
                userId,
                otherId);

        return ids.stream()
                .map(this::getById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }
}
