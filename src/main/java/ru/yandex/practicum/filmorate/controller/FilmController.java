package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable int id) {
        return filmService.getById(id);
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление нового фильма: {}", film);
        return filmService.add(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, id);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        log.info("Пользователь {} удаляет лайк фильму {}", userId, id);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        log.info("Получение популярных фильмов, count={}", count);
        return filmService.getPopular(count);
    }
}
