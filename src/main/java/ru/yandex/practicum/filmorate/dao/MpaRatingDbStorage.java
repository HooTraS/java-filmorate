package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper rowMapper;

    public Collection<MpaRating> getAll() {
        return jdbcTemplate.query("SELECT * FROM mpa ORDER BY mpa_id", rowMapper);
    }

    public Optional<MpaRating> getById(int id) {
        return jdbcTemplate.query("SELECT * FROM mpa WHERE mpa_id = ?", rowMapper, id)
                .stream()
                .findFirst();
    }
}
