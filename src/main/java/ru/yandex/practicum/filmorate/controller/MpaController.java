package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaRatingService service;

    @GetMapping
    public Collection<MpaRating> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable int id) {
        return service.getById(id);
    }
}
