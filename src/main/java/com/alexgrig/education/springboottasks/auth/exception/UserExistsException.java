package com.alexgrig.education.springboottasks.auth.exception;

import org.springframework.security.core.AuthenticationException;

//нужен для глобальной обработки всех ошибок аутентификации
public class UserExistsException extends AuthenticationException {

    public UserExistsException(String msg) {
        super(msg);
    }

    public UserExistsException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
