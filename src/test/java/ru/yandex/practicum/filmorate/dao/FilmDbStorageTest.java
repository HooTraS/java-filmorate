package ru.yandex.practicum.filmorate.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(FilmDbStorage.class)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Autowired
    public FilmDbStorageTest(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @Test
    void testAddAndGetById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film savedFilm = filmStorage.add(film);
        Optional<Film> loadedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(loadedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Inception")
                                .hasFieldOrPropertyWithValue("duration", 148)
                );
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Old Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Film savedFilm = filmStorage.add(film);
        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Desc");
        filmStorage.update(savedFilm);

        Optional<Film> updatedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Updated Film")
                                .hasFieldOrPropertyWithValue("description", "Updated Desc")
                );
    }
}