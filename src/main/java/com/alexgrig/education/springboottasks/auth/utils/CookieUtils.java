package com.alexgrig.education.springboottasks.auth.utils;

import org.apache.tomcat.util.http.SameSiteCookies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Component
public class CookieUtils {

    //private final String ACCESS_TOKEN = "access_token";

    @Value("${cookie.jwt.name}")
    private String cookieJwtName;

    @Value("${cookie.jwt.max-age}")
    private int cookieAccessTokenDuration;

    @Value("${cookie.domain}")
    private String cookieAccessTokenDomain;

    // создает server-side cookie со значением jwt.
    public HttpCookie createJwtCookie(String jwt) {
        return ResponseCookie
                // настройки кука
                .from(cookieJwtName, jwt) // название и значение кука
                .maxAge(cookieAccessTokenDuration) // 86400 сек = 1 сутки
                .sameSite(SameSiteCookies.STRICT.getValue()) // запрет на отправку кука, если запрос пришел со стороннего сайта (доп. защита от CSRF атак) - кук будет отправляться только если пользователь набрал URL в адресной строке
                .httpOnly(true) // кук будет доступен для считывания только на сервере (на клиенте НЕ будет доступен с помощью JavaScript - тем самым защищаемся от XSS атак)
                .secure(true) // кук будет передаваться браузером на backend только если канал будет защищен (https)
                .domain(cookieAccessTokenDomain) // для какого домена действует кук (перед отправкой запроса на backend - браузер "смотрит" на какой домен он отправляется - и если совпадает со значением из кука - тогда прикрепляет кук к запросу)
                .path("/") // кук будет доступен для всех URL

                // создание объекта
                .build();

        /* примечание: все настройки кука (domain, path и пр.) - влияют на то, будет ли браузер отправлять их при запросе.
            Браузер сверяет URL запроса (который набрали в адресной строке или любой ajax запрос с формы) с параметрами кука.
            И если есть хотя бы одно несовпадение (например domain или path) - кук отправлен не будет.
        */
    }

    // получает значение кук access_token и возвращает его значение (JWT)
    public String getCookieAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieJwtName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    //зануляет (удаляет) кук
    public HttpCookie deleteJwtCookie() {
        return ResponseCookie.from(cookieJwtName, null)
                .maxAge(0)
                .sameSite(SameSiteCookies.STRICT.getValue())
                .httpOnly(true)
                .secure(true)
                .domain(cookieAccessTokenDomain)
                .path("/")
                .build();
    }


}
