package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper rowMapper;

    public Collection<Genre> getAll() {
        return jdbcTemplate.query("SELECT * FROM GENRES ORDER BY genre_id", rowMapper);
    }

    public Optional<Genre> getById(int id) {
        return jdbcTemplate.query("SELECT * FROM GENRES WHERE genre_id = ?", rowMapper, id)
                .stream()
                .findFirst();
    }
}
