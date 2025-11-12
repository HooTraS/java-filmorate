package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<MpaRating> getAll() {
        return Arrays.asList(MpaRating.values());
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable int id) {
        MpaRating[] values = MpaRating.values();
        if (id < 1 || id > values.length) {
            throw new IllegalArgumentException("MPA с id=" + id + " не найден");
        }
        return values[id - 1];
    }
}
