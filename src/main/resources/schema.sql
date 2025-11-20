CREATE TABLE IF NOT EXISTS MPA_RATINGS (
                                           mpa_id INT AUTO_INCREMENT PRIMARY KEY,
                                           name VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS GENRES (
                                      genre_id INT AUTO_INCREMENT PRIMARY KEY,
                                      name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS FILMS (
                                     film_id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL,
                                     description VARCHAR(200),
                                     release_date DATE NOT NULL,
                                     duration INT NOT NULL,
                                     mpa_id INT,
                                     CONSTRAINT fk_mpa FOREIGN KEY (mpa_id)
                                         REFERENCES MPA_RATINGS(mpa_id)
);

CREATE TABLE IF NOT EXISTS USERS (
                                     user_id INT AUTO_INCREMENT PRIMARY KEY,
                                     email VARCHAR(100) NOT NULL UNIQUE,
                                     login VARCHAR(50) NOT NULL,
                                     name VARCHAR(100),
                                     birthday DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS LIKES (
                                     film_id INT NOT NULL,
                                     user_id INT NOT NULL,
                                     PRIMARY KEY (film_id, user_id),
                                     FOREIGN KEY (film_id) REFERENCES FILMS(film_id) ON DELETE CASCADE,
                                     FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS FILM_GENRES (
                                           film_id INT NOT NULL,
                                           genre_id INT NOT NULL,
                                           PRIMARY KEY (film_id, genre_id),
                                           CONSTRAINT fk_film FOREIGN KEY (film_id) REFERENCES FILMS(film_id),
                                           CONSTRAINT fk_genre FOREIGN KEY (genre_id) REFERENCES GENRES(genre_id)
);

CREATE TABLE IF NOT EXISTS FRIENDS (
                                       user_id INT NOT NULL,
                                       friend_id INT NOT NULL,
                                       status BOOLEAN,
                                       PRIMARY KEY (user_id, friend_id),
                                       FOREIGN KEY (user_id) REFERENCES USERS(user_id) ON DELETE CASCADE,
                                       FOREIGN KEY (friend_id) REFERENCES USERS(user_id) ON DELETE CASCADE
);
