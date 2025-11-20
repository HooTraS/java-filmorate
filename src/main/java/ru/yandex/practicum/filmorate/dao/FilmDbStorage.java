package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
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
    private final GenreRowMapper genreRowMapper;

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO FILMS (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        updateFilmGenres(film);

        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE FILMS SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        updateFilmGenres(film);
        return getById(film.getId()).orElseThrow();
    }

    private void updateFilmGenres(Film film) {
        jdbcTemplate.update("DELETE FROM FILM_GENRES WHERE film_id = ?", film.getId());

        if (film.getGenres() == null) {
            return;
        }

        String sql = "INSERT INTO FILM_GENRES (film_id, genre_id) VALUES (?, ?)";
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update(sql, film.getId(), genre.getId());
        }
    }

    @Override
    public Optional<Film> getById(int filmId) {
        String sql = """
                SELECT f.*, m.mpa_id, m.name AS mpa_name
                FROM FILMS f
                JOIN MPA_RATINGS m ON f.mpa_id = m.mpa_id
                WHERE f.film_id = ?
                """;

        Optional<Film> film = jdbcTemplate.query(sql, filmRowMapper, filmId)
                .stream()
                .findFirst();

        film.ifPresent(f -> f.setGenres(getGenresByFilmId(filmId)));

        return film;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = """
                SELECT f.*, m.mpa_id, m.name AS mpa_name
                FROM FILMS f
                JOIN MPA_RATINGS m ON f.mpa_id = m.mpa_id
                """;

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(f -> f.setGenres(getGenresByFilmId(f.getId())));

        return films;
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = """
                SELECT g.genre_id, g.name
                FROM FILM_GENRES fg
                JOIN GENRES g ON fg.genre_id = g.genre_id
                WHERE fg.film_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, genreRowMapper, filmId));
    }
}
