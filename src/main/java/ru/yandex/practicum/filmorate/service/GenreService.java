package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.DbGenreStorage;

import java.util.List;

@Service
public class GenreService {
    private final DbGenreStorage dbGenreStorage;

    @Autowired
    public GenreService(DbGenreStorage dbGenreStorage) {
        this.dbGenreStorage = dbGenreStorage;
    }

    public List<Genre> getAllGenres() {
        return dbGenreStorage.getAllGenres();
    }

    public Genre getGenreById(long id) {
        return dbGenreStorage.getGenreById(id);
    }
}
