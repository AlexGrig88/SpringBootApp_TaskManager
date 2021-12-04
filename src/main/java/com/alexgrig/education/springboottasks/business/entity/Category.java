package com.alexgrig.education.springboottasks.business.entity;

import com.alexgrig.education.springboottasks.auth.entity.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;


// Используем аннотации Hibernate: https://hibernate.org/orm/documentation/5.6/
@Entity
@EqualsAndHashCode
@NoArgsConstructor
@Getter
@Setter
public class Category {

    // указываем, что поле заполняется в БД
    // нужно, когда добавляем новый объект и он возвращается уже с новым id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    //всегда указывать аннотацию @Column, даже если у него нет параметров. Для того, чтобы Hibernate однозначно понимал какое поле связать со столбцом таблицы.
    @Column
    private String title;

    @Column(name = "completed_count", updatable = false) // т.к. это поле высчитывается автоматически в триггерах - вручную его не обновляем (updatable = false)
    private Long completedCount;

    @Column(name = "uncompleted_count", updatable = false) // т.к. это поле высчитывается автоматически в триггерах - вручную его не обновляем (updatable = false)
    private Long uncompletedCount;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // по каким полям связывать (foreign key)
    private User user;

    // не создаем обратную ссылку на Task с типом Collection, чтобы каждый раз не тянуть с объектом целую коллекцию - будет перегруз ненужных данных или зацикливание


}
