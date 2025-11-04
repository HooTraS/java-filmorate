package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    void testAddAndGetById() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);
        film.setMpa(MpaRating.PG_13); // ✅ Устанавливаем рейтинг, чтобы не было NPE

        Film savedFilm = filmStorage.add(film);
        Optional<Film> loadedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(loadedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Inception")
                                .hasFieldOrPropertyWithValue("description", "A mind-bending thriller")
                                .hasFieldOrPropertyWithValue("duration", 148)
                                .hasFieldOrPropertyWithValue("mpa", MpaRating.PG_13)
                );
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Old Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(MpaRating.G);

        Film savedFilm = filmStorage.add(film);

        savedFilm.setName("Updated Film");
        savedFilm.setDescription("Updated Desc");
        savedFilm.setMpa(MpaRating.R);

        filmStorage.update(savedFilm);

        Optional<Film> updatedFilm = filmStorage.getById(savedFilm.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f)
                                .hasFieldOrPropertyWithValue("name", "Updated Film")
                                .hasFieldOrPropertyWithValue("description", "Updated Desc")
                                .hasFieldOrPropertyWithValue("mpa", MpaRating.R)
                );
    }
}
