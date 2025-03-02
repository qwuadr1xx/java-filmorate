# java-filmorate
Template repository for Filmorate project.

https://dbdiagram.io/d/filmorate-67ab8281263d6cf9a0cc793c

Ниже будет приложен код для подтверждения дружбы при помощи обоюдной заявки

if (!isFriendshipExists(friendId, id)) {
log.error("Запись о дружбе не существует между пользователем с id {} и другом с id {}", friendId, id);
throw new EmptyResultDataAccessException(1);
} else {
jdbcTemplate.update(REMOVE_FRIEND, id, friendId);
log.debug("Запись о дружбе удалена для пары ({}; {})", id, friendId);
}
if (isFriendshipExists(id, friendId) && hasFriend(id, friendId)) {
log.debug("Обновляем обратный статус дружбы для пары ({}; {}) на false", friendId, id);
jdbcTemplate.update(UPDATE_FRIEND_STATUS, false, friendId, id);
}
User friend = getById(friendId);
log.info("Пользователь с id {} удалён из друзей.", friendId);
return friend;
это код для добавления друзей с подтверждением дружбы

if (!isFriendshipExists(friendId, id)) {
log.error("Запись о дружбе не существует между пользователем с id {} и другом с id {}", friendId, id);
throw new EmptyResultDataAccessException(1);
} else {
jdbcTemplate.update(REMOVE_FRIEND, id, friendId);
log.debug("Запись о дружбе удалена для пары ({}; {})", id, friendId);
}
if (isFriendshipExists(id, friendId) && hasFriend(id, friendId)) {
log.debug("Обновляем обратный статус дружбы для пары ({}; {}) на false", friendId, id);
jdbcTemplate.update(UPDATE_FRIEND_STATUS, false, friendId, id);
}
User friend = getById(friendId);
log.info("Пользователь с id {} удалён из друзей.", friendId);
return friend;
это код для удаления друзей с подтверждением дружбы
