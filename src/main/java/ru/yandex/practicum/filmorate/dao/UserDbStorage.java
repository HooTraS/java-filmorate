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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            user.setId(keyHolder.getKey().intValue());
        }
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                java.sql.Date.valueOf(user.getBirthday()), user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        Optional<User> userOptional = jdbcTemplate.query(sql, userRowMapper, id).stream().findFirst();
        // загружаем друзей в объект User (если нужно)
        userOptional.ifPresent(u -> {
            List<Integer> friendIds = jdbcTemplate.query(
                    "SELECT friend_id FROM friends WHERE user_id = ?",
                    (rs, rn) -> rs.getInt("friend_id"),
                    id
            );
            if (u.getFriends() == null) u.setFriends(new java.util.HashMap<>());
            // статус хранится как boolean в БД: true -> CONFIRMED, false -> UNCONFIRMED
            for (Integer fid : friendIds) {
                Boolean status = jdbcTemplate.queryForObject(
                        "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                        Boolean.class, id, fid);
                u.getFriends().put(fid, status != null && status ? FriendshipStatus.CONFIRMED : FriendshipStatus.UNCONFIRMED);
            }
        });
        return userOptional;
    }

    @Override
    public Collection<User> getAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    // --- friends methods implemented in DB version ---

    @Override
    public void addFriend(int userId, int friendId) {
        // если уже встречная запись есть и она UNCONFIRMED -> установим обе CONFIRMED
        Boolean reverseExists = Boolean.FALSE;
        try {
            reverseExists = jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                    Boolean.class, friendId, userId);
        } catch (Exception ignored) {

        }

        if (reverseExists == null) reverseExists = false;

        if (reverseExists == Boolean.FALSE) {
            // вставляем/обновляем собственную запись как UNCONFIRMED (false)
            jdbcTemplate.update("MERGE INTO friends(user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)",
                    userId, friendId, false);
        }

        // если встречная запись есть и была false -> делаем обе true
        try {
            Boolean rev = jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                    Boolean.class, friendId, userId);
            if (rev != null && rev == Boolean.FALSE) {
                jdbcTemplate.update("UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?", true, friendId, userId);
                jdbcTemplate.update("MERGE INTO friends(user_id, friend_id, status) KEY(user_id, friend_id) VALUES (?, ?, ?)",
                        userId, friendId, true);
            }
        } catch (Exception ignored) {
            // ничего
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? AND friend_id = ?", userId, friendId);
        // если есть встречная запись — пометим её как UNCONFIRMED (false)
        try {
            Boolean rev = jdbcTemplate.queryForObject(
                    "SELECT status FROM friends WHERE user_id = ? AND friend_id = ?",
                    Boolean.class, friendId, userId);
            if (rev != null && rev) {
                jdbcTemplate.update("UPDATE friends SET status = ? WHERE user_id = ? AND friend_id = ?", false, friendId, userId);
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public Collection<User> getFriends(int userId) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT friend_id FROM friends WHERE user_id = ?",
                (rs, rn) -> rs.getInt("friend_id"),
                userId
        );
        return ids.stream().map(this::getById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT f1.friend_id FROM friends f1 " +
                        "JOIN friends f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ?",
                (rs, rn) -> rs.getInt("friend_id"),
                userId, otherId
        );
        return ids.stream().map(this::getById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
}
