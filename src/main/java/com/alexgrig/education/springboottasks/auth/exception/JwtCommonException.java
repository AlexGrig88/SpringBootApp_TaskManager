package com.alexgrig.education.springboottasks.auth.exception;

import org.springframework.security.core.AuthenticationException;

// ошибка при валидации jwt
public class JwtCommonException extends AuthenticationException {

    public JwtCommonException(String msg) {
        super(msg);
    }

    public JwtCommonException(String msg, Throwable t) {
        super(msg, t);
    }
}
