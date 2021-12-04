package com.alexgrig.education.springboottasks.auth.exception;

import org.springframework.security.core.AuthenticationException;

// пользователь уже активирован
public class UserAlreadyActivatedException extends AuthenticationException {

    public UserAlreadyActivatedException(String msg) {
        super(msg);
    }

    public UserAlreadyActivatedException(String msg, Throwable t) {
        super(msg, t);
    }
}
