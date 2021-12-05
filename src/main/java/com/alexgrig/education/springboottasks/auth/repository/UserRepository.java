package com.alexgrig.education.springboottasks.auth.repository;

import com.alexgrig.education.springboottasks.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //для проверки существования email или username возвращаем true или false(весь объект возвращать нет смысла)
    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    @Query("select case when count(u) > 0 then true else false end from User u where lower(u.username) = lower(:username)")
    boolean existsByUsername(@Param("username") String username);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // обновление пароля
    @Modifying // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Transactional // если запрос изменяет данные - желательно добавлять эту аннотацию
    @Query("UPDATE User u SET u.password = :password WHERE u.username=:username") // обновление записи с нужным UUID
    int updatePassword(@Param("password") String password, @Param("username") String username); // возвращает int (сколько записей обновил) - в данном случае всегда должен возвращать 1
}
