package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.FeedRecord;

public interface FeedStorage {
    FeedRecord getRecord(long id);

    void setRecord(FeedRecord record);
}
