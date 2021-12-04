package com.alexgrig.education.springboottasks.business.repository;

import com.alexgrig.education.springboottasks.business.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

// Spring создаёт (а затем внедряет) объекты, реализующие CategoryRepository и готовые реализации методов из JpaRepository
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
// а также создаёт реализации(со сформированными sql запросами, естественно) по названию созданных нами методов (необходимо
// придерживаться правил создания имён методов:
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.query-creation
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    //поиск категорий пользователя по email (объект user с полем email)
    List<Category> findByUserEmailOrderByTitleAsc(String email);

    //поиск значений по названию для конкретного пользователя (JPQL - Java Persistence Query Language)
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query
    @Query("select c from Category c where " +

            "(:title is null or :title='' " +  //если передадим title пустым, то выбирутся все записи
            "or lower(c.title) like lower(concat('%', :title, '%')))" + //выбирутся только совпадающие записи

            "and c.user.email=:email " +
            "order by c.title asc" )
    List<Category> find(@Param("title") String title, @Param("email") String email);


}
