DELETE
FROM MPA_RATINGS;
DELETE
FROM GENRES;
ALTER TABLE MPA_RATINGS ALTER COLUMN id RESTART WITH 1;
ALTER TABLE GENRES ALTER COLUMN id RESTART WITH 1;

INSERT INTO genres (name) VALUES ('Комедия');
INSERT INTO genres (name) VALUES ('Драма');
INSERT INTO genres (name) VALUES ('Мультфильм');
INSERT INTO genres (name) VALUES ('Триллер');
INSERT INTO genres (name) VALUES ('Документальный');
INSERT INTO genres (name) VALUES ('Боевик');

INSERT INTO mpa_ratings (name) VALUES ('G');
INSERT INTO mpa_ratings (name) VALUES ('PG');
INSERT INTO mpa_ratings (name) VALUES ('PG-13');
INSERT INTO mpa_ratings (name) VALUES ('R');
INSERT INTO mpa_ratings (name) VALUES ('NC-17');