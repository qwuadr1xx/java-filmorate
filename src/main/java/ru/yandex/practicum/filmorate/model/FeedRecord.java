package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.enums.EventType;
import ru.yandex.practicum.filmorate.enums.Operation;

@Builder(toBuilder = true)
@Data
public class FeedRecord {
    private final long timestamp;

    private final Long userId;

    private final EventType eventType;

    private final Operation operation;

    private final Long eventId;

    private final Long entityId;
}
