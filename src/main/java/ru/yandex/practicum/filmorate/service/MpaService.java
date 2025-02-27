package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.DbMpaStorage;

import java.util.List;

@Service
public class MpaService {
    private final DbMpaStorage dbMpaStorage;

    @Autowired
    public MpaService(DbMpaStorage dbMpaStorage) {
        this.dbMpaStorage = dbMpaStorage;
    }

    public List<Mpa> getAllMpa() {
        return dbMpaStorage.getAllMpaRatings();
    }

    public Mpa getMpaById(long id) {
        return dbMpaStorage.getMpaRatingById(id);
    }
}
