package com.alexgrig.education.springboottasks.auth.filter;

import com.alexgrig.education.springboottasks.auth.entity.User;
import com.alexgrig.education.springboottasks.auth.exception.JwtCommonException;
import com.alexgrig.education.springboottasks.auth.service.UserDetailsImpl;
import com.alexgrig.education.springboottasks.auth.utils.CookieUtils;
import com.alexgrig.education.springboottasks.auth.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private JwtUtils jwtUtils;
    private CookieUtils cookieUtils;

    // допустимые URI, для которых не требуется авторизация (не будет проверяться наличие jwt-кука)
    private List<String> permitURL = Arrays.asList(
            "register", // регистрация нового пользователя
            "login", // аутентификация (логин-пароль)
            "activate-account", // активация нового пользователя
            "resend-activate-email", // запрос о повторной отправки письма активации
            "send-reset-password-email", // запрос на отправку письма об обновлении пароля
            "test-no-auth", // если есть какой-либо тестовый URL для проверки работы backend
            "index" // если есть отдельная главная страница

            // можно добавлять сюда любые открытые URL
    );

    // конструктор с @Autowired не можем использовать, т.к. компонент должен уметь создаваться с пустым конструктором
    @Autowired
    public void setJwtUtils(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    public void setCookieUtils(CookieUtils cookieUtils) {
        this.cookieUtils = cookieUtils;
    }


    // этот метод вызывается автоматически при каждом входящем запросе
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Проверяем, запрос идет на публичную страницу или нет.

        boolean isRequestToPublicAPI = permitURL.stream()
                .anyMatch(s -> request.getRequestURI().toLowerCase().contains(s));

        if (!isRequestToPublicAPI
                //&&
                //SecurityContextHolder.getContext().getAuthentication() == null  // если пользователь еще не прошел аутентификацию (а значит объект Authentication == null в контейнере Spring, вдруг ранее еще где-то уже произвели аутентификацию)
        ) {
            // сюда попадем, если запрос хочет получить данные, которые требуют аутентификации, ролей и пр.
            String jwt = cookieUtils.getCookieAccessToken(request); // для всех остальных запросов - получаем jwt из кука

            if (jwt != null) { // если токен найден

                if (jwtUtils.validate(jwt)) {  // если токен успешно прошел валидацию - значит пользователь до этого уже успешно вошел в систему (ввел логин-пароль) и получил свой JWT

                    /*
                    Теперь нужно считать все данные пользователя из JWT, чтобы получить userDetails, добавить его в Spring контейнер (авторизовать) и не делать ни одного запроса в БД
                    Запрос в БД выполняем только 1 раз, когда пользователь залогинился. После этого аутентификация/авторизация проходит автоматически с помощью JWT
                    Мы должны создать объект userDetails на основе данных JWT (все поля, кроме пароля)
                    Здесь мы не используем UserDetailsService, т.к не нужно выполнять запросы в БД
                     */
                    User user = jwtUtils.getUser(jwt);

                    UserDetailsImpl userDetails = new UserDetailsImpl(user);

                    // Вручную создаем объект UsernamePasswordAuthenticationToken (т.е. не используем пароль и не вызываем метод authenticate, как в методе login - это уже сделано ранее и был создан jwt)
                    // Привязываем UsernamePasswordAuthenticationToken к пользователю
                    // Добавляем объект UsernamePasswordAuthenticationToken в Spring контейнер - тем самым Spring будет видеть, что к пользователю привязан объект authentication - соответственно он успешно залогинен
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()); //пароль не нужен

                    // 1) добавляем входящий запрос в контейнер, чтобы дальше уже Spring обрабатывал запрос с учетом данных авторизации
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // 2) добавляем объект authentication в spring контейнер - тем самым Spring поймет, что пользователь успешно залогинен
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    System.out.println("jwt = " + jwt);


                } else { // не смогли обработать токен (возможно вышел срок действия или любая другая ошибка)
                    throw new JwtCommonException("jwt validate exception"); // пользователь не будет авторизован (т.к. jwt некорректный) и клиенту отправится ошибка
                }
            } else {
                throw new AuthenticationCredentialsNotFoundException("token not found"); // если запрос пришел не на публичную страницу и если не найден jwt
            }
        }

        /* сюда дойдем только в 2х случаях:
         1) если запрос пришел на публичную ссылку (не требует авторизации)
         2) если запрос пришел на закрытую ссылку и jwt прошел валидацию (срок действия и пр.) - а значит пользователь уже авторизован в Spring контейнере
         */

        filterChain.doFilter(request, response); // продолжить выполнение запроса (запрос отправится дальше в контроллер)
    }
}
