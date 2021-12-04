package com.alexgrig.education.springboottasks.auth.exception;

import org.springframework.security.core.AuthenticationException;

// указанная роль пользователя не найдена в БД
public class RoleNotFoundException extends AuthenticationException {

    public RoleNotFoundException(String msg) {
        super(msg);
    }

    public RoleNotFoundException(String msg, Throwable t) {
        super(msg, t);
    }
}

