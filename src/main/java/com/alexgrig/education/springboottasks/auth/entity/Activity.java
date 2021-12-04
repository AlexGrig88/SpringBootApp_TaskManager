package com.alexgrig.education.springboottasks.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/*

Вся активность пользователя (активация аккаунта, другие действия по необходимости)

*/


@Setter
@Getter
@DynamicUpdate
@Entity
public class Activity { // название таблицы будет браться автоматически по названию класса с маленькой буквы: activity

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Type(type = "org.hibernate.type.NumericBooleanType") // для автоматической конвертации числа в true/false
    @Column
    private boolean activated; // становится true только после подтверждения активации пользователем (обратно false уже стать не может по логике)

    @NotBlank
    @Column(updatable = false)
    private String uuid; // создается только один раз

    @JsonIgnore// чтобы не было бесконечного обратного зацикливания (JSON не сможет сформироваться) - ссылку на Activity для JSON имеем только из User
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

}
