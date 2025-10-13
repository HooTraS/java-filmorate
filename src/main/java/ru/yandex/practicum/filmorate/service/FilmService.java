package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Film add(Film film) {
        validate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validate(film);
        filmStorage.getById(film.getId())
                .orElseThrow(() -> new ValidationException("Фильм с таким id не найден"));
        return filmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new ValidationException("Фильм с таким id не найден"));
    }

    public void addLike(int filmId, int userId) {
        Film film = getById(filmId);
        likes.computeIfAbsent(film.getId(), k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {
        Set<Integer> filmLikes = likes.get(filmId);
        if (filmLikes != null) {
            filmLikes.remove(userId);
        }
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        likes.getOrDefault(f2.getId(), Set.of()).size(),
                        likes.getOrDefault(f1.getId(), Set.of()).size()))
                .limit(count)
                .toList();
    }

    private void validate(Film film) {
        if (film.getName() == null || film.getName().isBlank())
            throw new ValidationException("Название фильма не может быть пустым");
        if (film.getDescription() != null && film.getDescription().length() > 200)
            throw new ValidationException("Описание не может превышать 200 символов");
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE))
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        if (film.getDuration() <= 0)
            throw new ValidationException("Продолжительность фильма должна быть положительной");
    }
}
