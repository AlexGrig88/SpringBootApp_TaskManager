package com.alexgrig.education.springboottasks.auth.filter;

import com.alexgrig.education.springboottasks.auth.customobjects.JsonException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//перехватывает ошибки всех фильтров после текущего
//оборачивает в json и отправляет клиенту
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response); //вызов следующего по цепочке фильтра
        } catch (RuntimeException e) {
            //создать JSON и отправить название класса ошибки
            JsonException ex = new JsonException(e.getClass().getSimpleName());

            response.setStatus(HttpStatus.UNAUTHORIZED.value()); //неавторизован для данного действия
            response.getWriter().write(convertObjectToJson(ex));
        }
    }

    private String convertObjectToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper(); //объект из библиотеки jackson для формирования json
        return mapper.writeValueAsString(object);

    }
}
