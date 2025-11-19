package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("Inception");
        film.setDescription("Mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        MpaRating mpa = new MpaRating();
        mpa.setId(3);
        mpa.setName("PG-13");
        film.setMpa(mpa);

        // Мокаем успешное добавление фильма для валидных случаев
        when(filmService.add(any(Film.class))).thenReturn(film);
    }

    @Test
    void shouldAddValidFilm() throws Exception {
        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldThrowWhenNameIsEmpty() throws Exception {
        film.setName("");

        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowWhenDescriptionTooLong() throws Exception {
        film.setDescription("A".repeat(201));

        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowWhenReleaseDateTooEarly() throws Exception {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowWhenDurationZero() throws Exception {
        film.setDuration(0);

        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldThrowWhenDurationNegative() throws Exception {
        film.setDuration(-10);

        mockMvc.perform(
                        post("/films")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(film))
                )
                .andExpect(status().isBadRequest());
    }
}