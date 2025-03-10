package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedRecord;

import java.util.List;

public interface FeedStorage {
    List<FeedRecord> getRecord(long id);

    void setRecord(FeedRecord record);
}
