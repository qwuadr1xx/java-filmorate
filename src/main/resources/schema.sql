DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_rating CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS feed CASCADE;

CREATE TABLE IF NOT EXISTS mpa_ratings (
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS genres (
    id   INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
    id            INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name          VARCHAR(64)  NOT NULL,
    description   VARCHAR(200) NOT NULL,
    release_date  DATE         NOT NULL,
    duration      INTEGER      NOT NULL,
    mpa_rating_id INTEGER      NOT NULL,
    FOREIGN KEY (mpa_rating_id) REFERENCES mpa_ratings (id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id  INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id)  REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE IF NOT EXISTS users (
    id       INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email    VARCHAR(256) NOT NULL,
    login    VARCHAR(64)  NOT NULL,
    name     VARCHAR(64)  NOT NULL,
    birthday DATE         NOT NULL
);

CREATE TABLE IF NOT EXISTS likes (
    film_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (id),
    FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS friendship (
    user_id       INTEGER NOT NULL,
    friend_id     INTEGER NOT NULL,
    friend_status BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (friend_id) REFERENCES users (id)
);

CREATE TABLE IF NOT EXISTS feed (
    timestamp TIMESTAMP NOT NULL,
    user_id INTEGER NOT NULL,
    event_type VARCHAR NOT NULL,
    operation VARCHAR NOT NULL,
    event_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    entity_id INTEGER NOT NULL
);
