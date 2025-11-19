package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage storage;

    public Collection<Genre> getAll() {
        return storage.getAll();
    }

    public Genre getById(int id) {
        return storage.getById(id)
                .orElseThrow(() -> new NotFoundException("Жанр не найден"));
    }
}

