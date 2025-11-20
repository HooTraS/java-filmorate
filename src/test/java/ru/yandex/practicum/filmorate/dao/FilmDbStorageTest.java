package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM FILM_GENRES");
        jdbcTemplate.update("DELETE FROM LIKES");
        jdbcTemplate.update("DELETE FROM FILMS");
        jdbcTemplate.update("DELETE FROM MPA_RATINGS");

        jdbcTemplate.update("INSERT INTO MPA_RATINGS (mpa_id, name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO MPA_RATINGS (mpa_id, name) VALUES (2, 'PG')");
        jdbcTemplate.update("INSERT INTO MPA_RATINGS (mpa_id, name) VALUES (3, 'PG-13')");
        jdbcTemplate.update("INSERT INTO MPA_RATINGS (mpa_id, name) VALUES (4, 'R')");
        jdbcTemplate.update("INSERT INTO MPA_RATINGS (mpa_id, name) VALUES (5, 'NC-17')");
    }

    @Test
    void testAddAndGetById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        mpa.setName("PG-13");
        film.setMpa(mpa);

        Film savedFilm = filmStorage.add(film);
        Optional<Film> loadedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(loadedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Inception")
                                .hasFieldOrPropertyWithValue("description", "A mind-bending thriller")
                                .hasFieldOrPropertyWithValue("duration", 148)
                                .extracting(Film::getMpa)
                                .extracting(MpaRating::getId)
                                .isEqualTo(3)
                );
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Old Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Film savedFilm = filmStorage.add(film);

        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Desc");
        savedFilm.setDuration(120);

        MpaRating updatedMpa = new MpaRating();
        updatedMpa.setId(4);
        updatedMpa.setName("R");
        savedFilm.setMpa(updatedMpa);

        filmStorage.update(savedFilm);

        Optional<Film> updatedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Updated Film")
                                .hasFieldOrPropertyWithValue("description", "Updated Desc")
                                .hasFieldOrPropertyWithValue("duration", 120)
                                .extracting(Film::getMpa)
                                .extracting(MpaRating::getId)
                                .isEqualTo(4)
                );
    }
}