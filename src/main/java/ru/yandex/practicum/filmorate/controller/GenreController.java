package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    @GetMapping
    public List<Genre> getAll() {
        return Arrays.asList(Genre.values());
    }

    @GetMapping("/{id}")
    public Genre getById(@PathVariable int id) {
        Genre[] values = Genre.values();
        if (id < 1 || id > values.length) {
            throw new IllegalArgumentException("Жанр с id=" + id + " не найден");
        }
        return values[id - 1];
    }
}
