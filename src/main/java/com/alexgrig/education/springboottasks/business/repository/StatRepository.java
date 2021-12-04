package com.alexgrig.education.springboottasks.business.repository;

import com.alexgrig.education.springboottasks.business.entity.Stat;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatRepository extends CrudRepository<Stat, Long> {

    Stat findByUserEmail(String email); // возвращается только 1 запись (каждый пользователь содержит только 1 запись в таблице Stat)
}

