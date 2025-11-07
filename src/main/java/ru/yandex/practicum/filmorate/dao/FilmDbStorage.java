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
            // film.getReleaseDate() — должен быть LocalDate
            ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            // сохраняем строковое имя рейтинга (или null)
            ps.setString(5, film.getMpa() != null ? film.getMpa().name() : null);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            film.setId(keyHolder.getKey().intValue());
        }

        updateFilmGenres(film); // helper для записи в film_genres

        // возвращаем актуальную запись, чтобы mpa/genres корректно заполнились RowMapper'ом
        return getById(film.getId()).orElseThrow();
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().name() : null,
                film.getId()
        );

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        updateFilmGenres(film);

        return getById(film.getId()).orElseThrow();
    }

    private void updateFilmGenres(Film film) {
        if (film.getGenres() != null) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbcTemplate.update(sql, film.getId(), genre.getId());
            }
        }
    }

    @Override
    public Optional<Film> getById(int filmId) {
        String sql = "SELECT * FROM films WHERE film_id = ?";

        Optional<Film> filmOptional = jdbcTemplate.query(sql, filmRowMapper, filmId)
                .stream()
                .findFirst();

        filmOptional.ifPresent(film -> film.setGenres(getGenresByFilmId(filmId)));

        return filmOptional;
    }

    @Override
    public Collection<Film> getAll() {
        String sql = "SELECT * FROM films";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
        }

        return films;
    }

    private Set<Genre> getGenresByFilmId(int filmId) {
        String sql = "SELECT g.genre_id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";

        return new HashSet<>(jdbcTemplate.query(sql,
                (rs, rowNum) -> Genre.fromId(rs.getInt("genre_id")),
                filmId));
    }
}
