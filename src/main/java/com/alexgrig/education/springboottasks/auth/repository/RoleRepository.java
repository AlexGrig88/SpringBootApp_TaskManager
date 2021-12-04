package com.alexgrig.education.springboottasks.auth.repository;

import com.alexgrig.education.springboottasks.auth.entity.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends CrudRepository<Role, Long> {

    // возвращает контейнер Optional, в котором может быть объект или null
    Optional<Role> findByName(String name); // поиск роли по названию

}
