package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage) {
        this.directorStorage = directorStorage;
    }

    public List<Director> getDirectors() {
        return directorStorage.getAll();
    }

    public Director getDirectorById(Long id) {
        return directorStorage.getById(id);
    }

    public Director createDirector(Director director) {
        return directorStorage.create(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.update(director);
    }

    public void deleteDirector(Long id) {
        directorStorage.deleteById(id);
    }

    public void deleteDirector(Director director) {
        deleteDirector(director.getId());
    }

}
