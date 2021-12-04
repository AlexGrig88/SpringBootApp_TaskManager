package com.alexgrig.education.springboottasks.business.repository;

import com.alexgrig.education.springboottasks.business.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityRepository extends JpaRepository<Priority, Long> {

    // поиск значений по названию для конкретного пользователя
    @Query("SELECT p FROM Priority p where " +

            "(:title is null or :title='' " + // если передадим параметр title пустым, то выберутся все записи (сработает именно это условие)
            " or lower(p.title) like lower(concat('%', :title,'%'))) " + // если параметр title не пустой, то выполнится уже это условие

            " and p.user.email=:email " + // фильтрация для конкретного пользователя

            "order by p.title asc")
    List<Priority> find(@Param("title") String title, @Param("email") String email);


    // поиск категорий пользователя (по email)
    List<Priority> findByUserEmailOrderByIdAsc(String email);
}
