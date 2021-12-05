package com.alexgrig.education.springboottasks.auth.service;


import com.alexgrig.education.springboottasks.auth.entity.Activity;
import com.alexgrig.education.springboottasks.auth.entity.Role;
import com.alexgrig.education.springboottasks.auth.entity.User;
import com.alexgrig.education.springboottasks.auth.repository.ActivityRepository;
import com.alexgrig.education.springboottasks.auth.repository.RoleRepository;
import com.alexgrig.education.springboottasks.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    public static final String DEFAULT_ROLE = "USER"; // такая роль должна быть обязательно в таблице БД

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private ActivityRepository activityRepository;

    @Autowired
    public UserService(UserRepository userRepository, RoleRepository roleRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.activityRepository = activityRepository;
    }

    public void register(User user, Activity activity) {
        userRepository.save(user);
        activityRepository.save(activity);
    }

    //проверка, существует ли пользователь в бд (email and username must be unique)
    public boolean userExists(String email, String username) {
        return userRepository.existsByEmail(email) || userRepository.existsByUsername(username);
    }

    // получаем из БД объект роли
    public Optional<Role> findByName(String role) {
        return roleRepository.findByName(role);
    }

    public Optional<Activity> findActivityByUuid(String uuid){
        return activityRepository.findByUuid(uuid);
    }

    public Optional<Activity> findActivityByUserId(long id){
        return activityRepository.findByUserId(id);
    }


    // true сконвертируется в 1, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int activate(String uuid){
        return activityRepository.changeActivated(uuid, true);
    }

    // false сконвертируется в 0, т.к. указали @Type(type = "org.hibernate.type.NumericBooleanType") в классе Activity
    public int deactivate(String uuid){
        return activityRepository.changeActivated(uuid, false);
    }

    //обновление пароля
    public int updatePassword(String pass, String username) {
       return userRepository.updatePassword(pass, username);
    }

}
