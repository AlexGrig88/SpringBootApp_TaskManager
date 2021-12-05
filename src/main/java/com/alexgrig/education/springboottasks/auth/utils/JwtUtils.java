package com.alexgrig.education.springboottasks.auth.utils;

import com.alexgrig.education.springboottasks.auth.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

@Component
@Log
public class JwtUtils {

    public static final String CLAIM_USER_KEY = "user";

    @Value("${jwt.secret}")
    private String jwtSecret; // секретный ключ для создания jwt (хранится только на сервере, нельзя никуда передавать)


    @Value("${jwt.access_token-expiration}") // 86400000 мс = 1 сутки
    private int accessTokenExpiration; // длительность токена для автоматического логина (все запросы будут автоматически проходить аутентификацию, если в них присутствует JWT)
    // название взяли по аналогии с протоколом OAuth2, но не путайте - это просто название нашего JWT, здесь мы не применяем OAuth2

    @Value("${jwt.reset-pass-expiration}") // 300000 мс = 5 мин
    private int resetPassTokenExpiration; // длительность токена для сброса пароля (чем короче, тем лучше)

    // генерация JWT для доступа к данным
    public String createAccessToken(User user) { // в user будут заполнены те поля, которые нужны аутентификации пользователя и работы в системе
        return createToken(user, accessTokenExpiration);
    }


    // генерация JWT для сброса пароля
    public String createEmailResetToken(User user) { // в user будут заполнены только те поля, которые нужны для сброса пароля
        return createToken(user, resetPassTokenExpiration);
    }

    // генерация JWT по данным пользователя
    public String createToken(User user, int duration) {

        Date currentDate = new Date(); // для отсчета времени от текущего момента - для задания expiration
        // пароль зануляем до формирования jwt
        user.setPassword(null); // пароль нужен только один раз для аутентификации - поэтому можем его занулить, чтобы больше нигде не "засветился"

        Map<String, Object> claims = new HashMap<>();
        claims.put(CLAIM_USER_KEY, user);
        claims.put(Claims.SUBJECT, user.getId()); //ситемное стандартное поле sub, желательно добавлять

        return Jwts.builder()
                // задаем claims
                // Какие именно данные (claims) добавлять в JWT (решаете сами)
                //.setSubject((user.getId().toString())) // subject - это одно из стандартных полей jwt (сохраняем неизменяемое id пользователя)
                .setClaims(claims)
                .setIssuedAt(currentDate) // время отсчета - текущий момент
                .setExpiration(new Date(currentDate.getTime() + duration)) // срок действия access_token

                .signWith(SignatureAlgorithm.HS512, jwtSecret) // используем алгоритм кодирования HS512, хешируем все данные секретным ключом-строкой
                .compact(); // кодируем в формат Base64
    }

    // проверить целостность данных (не истек ли срок jwt и пр.)
    public boolean validate(String jwt) {
        try {
                Jwts
                    .parser() // проверка формата на корректность
                    .setSigningKey(jwtSecret) // указываем каким ключом будет проверять подпись
                    .parseClaimsJws(jwt); // проверка подписи "секретом"
            return true; // проверка прошла успешно

        } catch (MalformedJwtException e) {
            log.log(Level.SEVERE, "Invalid JWT token: ", jwt);
        } catch (ExpiredJwtException e) {
            log.log(Level.SEVERE, "JWT token is expired: ", jwt);
        } catch (UnsupportedJwtException e) {
            log.log(Level.SEVERE, "JWT token is unsupported: ", jwt);
        } catch (IllegalArgumentException e) {
            log.log(Level.SEVERE, "JWT claims string is empty: ", jwt);
        }

        return false; // валидация не прошла успешно (значит данные payload были изменены - подпись была наложена не на этот payload)

        /*
        Сервер проверяет своим ключом JWT.
        Если подпись не прошла проверку (failed) - значит эти данные были подписаны на нашим secret (или сами данные после подписи были изменены), а значит к данным нет доверия.
        Сервер может доверять только тем данным, которые подписаны его secret ключом. Этот ключ хранится только на сервере, а значит никто кроме сервера не мог им воспользоваться и подписать данные.
        */
    }

    public User getUser(String jwt) {
        Map map = (Map)Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(jwt).getBody().get(CLAIM_USER_KEY); //здесь это поле из токена

        ObjectMapper mapper = new ObjectMapper();
        User user = mapper.convertValue(map, User.class);

        return user;
    }

}
