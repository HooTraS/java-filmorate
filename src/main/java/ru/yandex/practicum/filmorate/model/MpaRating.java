package ru.yandex.practicum.filmorate.model;

public enum MpaRating {
    G(1, "G"),
    PG(2, "PG"),
    PG_13(3, "PG-13"),
    R(4, "R"),
    NC_17(5, "NC-17");

    private final int id;
    private final String label;

    MpaRating(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static MpaRating fromId(int id) {
        for (MpaRating m : values()) {
            if (m.id == id) return m;
        }
        throw new IllegalArgumentException("Unknown MPA id: " + id);
    }

    public static MpaRating fromName(String name) {
        return valueOf(name.replace("-", "_").replace(".", "_").toUpperCase());
    }
}
