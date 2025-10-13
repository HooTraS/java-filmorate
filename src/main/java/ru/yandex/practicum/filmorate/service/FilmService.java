package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film add(Film film) {
        validate(film);
        return filmStorage.add(film);
    }

    public Film update(Film film) {
        validate(film);
        filmStorage.getById(film.getId())
                .orElseThrow(() -> new ValidationException("Фильм с id=" + film.getId() + " не найден"));
        return filmStorage.update(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(int id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new ValidationException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(int filmId, int userId) {
        getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь с id=" + userId + " не найден"));

        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getById(filmId);
        userStorage.getById(userId)
                .orElseThrow(() -> new ValidationException("Пользователь с id=" + userId + " не найден"));

        Optional.ofNullable(likes.get(filmId)).ifPresent(s -> s.remove(userId));
    }

    public List<Film> getPopular(int count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> likes.getOrDefault(f.getId(), Set.of()).size()).reversed())
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
