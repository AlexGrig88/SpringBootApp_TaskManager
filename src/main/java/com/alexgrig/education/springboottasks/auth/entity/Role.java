package com.alexgrig.education.springboottasks.auth.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
@Table(name="ROLE_DATA") // явно указываем название таблицы, если оно отличается от названия класса с маленькой буквы
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name; // название роли
}
