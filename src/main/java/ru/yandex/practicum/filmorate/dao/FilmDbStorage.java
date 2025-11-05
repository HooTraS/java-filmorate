package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

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
            ps.setString(5, film.getMpa() != null ? film.getMpa().name() : null);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }

        // сохраняем жанры, если они есть
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.ordinal() + 1);
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()), film.getDuration(),
                film.getMpa().name(), film.getId());

        // сначала удаляем старые жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        // и записываем новые
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.ordinal() + 1);
            }
        }

        return film;
    }

    @Override
    public Optional<Film> getById(int filmId) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        Optional<Film> filmOptional = jdbcTemplate.query(sql, filmRowMapper, filmId)
                .stream()
                .findFirst();

        filmOptional.ifPresent(film -> {
            List<Genre> genres = jdbcTemplate.query(
                    "SELECT g.name FROM FILM_GENRES fg " +
                            "JOIN GENRES g ON fg.genre_id = g.genre_id " +
                            "WHERE fg.film_id = ?",
                    (rs, rowNum) -> Genre.valueOf(rs.getString("name").toUpperCase()),
                    filmId
            );

            film.setGenres(new HashSet<>(genres));
        });

        return filmOptional;
    }



    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        // добавляем жанры каждому фильму
        for (Film film : films) {
            film.setGenres((Set<Genre>) getGenresByFilmId(film.getId()));
        }

        return films;
    }

    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT genre_id FROM film_genres WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                Genre.values()[rs.getInt("genre_id") - 1], filmId);
    }
}
