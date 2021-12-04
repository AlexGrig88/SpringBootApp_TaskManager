package com.alexgrig.education.springboottasks.auth.repository;


import com.alexgrig.education.springboottasks.auth.entity.Activity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ActivityRepository extends CrudRepository<Activity, Long> {

    // активация/деактивация пользователя
    @Modifying // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Transactional // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Query("UPDATE Activity a SET a.activated = :active WHERE a.uuid=:uuid") // обновление записи с нужным UUID
    int changeActivated(@Param("uuid") String uuid, @Param("active") boolean active); // возвращает int (сколько записей обновил) - в данном случае всегда должен возвращать 1

    // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
    Optional<Activity> findByUserId(long id); // поиск по id

    // используем обертку Optional - контейнер, который хранит значение или null - позволяет избежать ошибки NullPointerException
    Optional<Activity> findByUuid(String uuid); // поиск по UUID
}
