package ru.yandex.practicum.filmorate.model;

public enum Genre {
    COMEDY(1, "COMEDY"),
    DRAMA(2, "DRAMA"),
    CARTOON(3, "CARTOON"),
    ACTION(4, "THRILLER"),
    THRILLER(5, "ACTION");

    private final int id;
    private final String name;

    Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public static Genre fromId(int id) {
        for (Genre genre : values()) {
            if (genre.id == id) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown genre id: " + id);
    }

    public static Genre fromName(String name) {
        for (Genre genre : values()) {
            if (genre.name.equalsIgnoreCase(name) || genre.name().equalsIgnoreCase(name)) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Unknown genre name: " + name);
    }
}