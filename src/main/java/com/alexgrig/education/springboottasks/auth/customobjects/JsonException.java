package com.alexgrig.education.springboottasks.auth.customobjects;

import lombok.Getter;
import lombok.Setter;

//POJO класс для передачи ошибки клиенту в формате JSON
@Getter
@Setter
public class JsonException {
    private String exception; //тип ошибки

    public JsonException(String exception) {
        this.exception = exception;
    }
}
