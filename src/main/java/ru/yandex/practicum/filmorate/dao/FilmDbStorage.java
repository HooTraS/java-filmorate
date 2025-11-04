package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setString(5, film.getMpa().name());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().name(), film.getId());
        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        return jdbcTemplate.query(sql, filmRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, filmRowMapper);
    }
}
