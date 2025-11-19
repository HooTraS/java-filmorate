package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage storage;

    public Collection<MpaRating> getAll() {
        return storage.getAll();
    }

    public MpaRating getById(int id) {
        return storage.getById(id)
                .orElseThrow(() -> new NotFoundException("MPA рейтинг не найден"));
    }
}
