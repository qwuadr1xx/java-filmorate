package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.enums.Entity;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films;
    private final Map<Long, Set<Long>> likes;
    private long nextId;

    InMemoryFilmStorage() {
        films = new HashMap<>();
        likes = new HashMap<>();
        nextId = 1;
    }

    @Override
    public Film create(Film film) {
        long localId = generateId();
        Film localFilm = film.toBuilder().id(localId).build();

        films.put(localId, localFilm);
        return localFilm;
    }

    @Override
    public Film update(Film film) {
        Long localId = film.getId();

        if (localId == null) {
            throw new BadRequestException("значение id равно null", Entity.FILM);
        }
        isIdExists(localId);

        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getById(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> getAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public void addLike(long id, long userId) {
        isIdExists(id);

        Set<Long> filmLikes;
        if (likes.containsKey(id)) {
            filmLikes = likes.get(id);
        } else {
            filmLikes = new HashSet<>();
        }

        filmLikes.add(userId);
        likes.put(id, filmLikes);
    }

    @Override
    public void removeLike(long id, long userId) {
        isIdExists(id);

        Set<Long> filmLikes;
        if (likes.containsKey(id)) {
            filmLikes = likes.get(id);
        } else {
            throw new BadRequestException("id с таким фильмом не существует", Entity.FILM);
        }

        filmLikes.remove(userId);
        likes.put(id, filmLikes);
    }

    @Override
    public void isIdExists(long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException(id, Entity.FILM);
        }
    }

    @Override
    public List<Film> getLikedFilms() {
        return new ArrayList<>(likes.keySet().stream()
                .sorted((id1, id2) -> Long.compare(likes.get(id2).size(), likes.get(id1).size())).map(films::get)
                .toList());
    }

    private long generateId() {
        return nextId++;
    }
}
