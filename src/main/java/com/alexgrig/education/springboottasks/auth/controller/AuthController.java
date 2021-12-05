package com.alexgrig.education.springboottasks.auth.controller;

import com.alexgrig.education.springboottasks.auth.customobjects.JsonException;
import com.alexgrig.education.springboottasks.auth.entity.Activity;
import com.alexgrig.education.springboottasks.auth.entity.Role;
import com.alexgrig.education.springboottasks.auth.entity.User;
import com.alexgrig.education.springboottasks.auth.exception.RoleNotFoundException;
import com.alexgrig.education.springboottasks.auth.exception.UserAlreadyActivatedException;
import com.alexgrig.education.springboottasks.auth.exception.UserExistsException;
import com.alexgrig.education.springboottasks.auth.service.UserDetailsImpl;
import com.alexgrig.education.springboottasks.auth.service.UserService;
import com.alexgrig.education.springboottasks.auth.utils.CookieUtils;
import com.alexgrig.education.springboottasks.auth.utils.JwtUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

import static com.alexgrig.education.springboottasks.auth.service.UserService.DEFAULT_ROLE;

@RestController
@RequestMapping("/auth")
@Log
public class AuthController {

    private UserService userService;
    private PasswordEncoder encoder; // кодировщик паролей (или любых данных), создает односторонний хеш
    private AuthenticationManager authenticationManager; // стандартный встроенный менеджер Spring, проверяет логин-пароль
    private JwtUtils jwtUtils; // класс-утилита для работы с jwt
    private CookieUtils cookieUtils; // класс-утилита для работы с куками

    @Autowired
    public AuthController(UserService userService, PasswordEncoder encoder,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils, CookieUtils cookieUtils) {
        this.userService = userService;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.cookieUtils = cookieUtils;

    }

    @PostMapping("/test-no-auth")
    public String testNoAuth() {
        return "OK-no-auth";
    }

    @PostMapping("/test-with-auth")
    @PreAuthorize("USER")
    public String testWithAuth() {
        return "OK-with-auth";
    }


    @PutMapping("/register")
    public ResponseEntity register(@Valid @RequestBody User user) {

        //если существует бросаем исключение
        if (userService.userExists(user.getEmail(), user.getUsername())) {
            throw new UserExistsException("User already exists");
        }

         /* нам не нужно создавать новый объект Role, а нужно получать его из БД (т.к. все роли уже созданы в таблицах)
           Объект роли нужно всегда получать из БД перед регистрацией пользователя, т.к. все должно работать динамически (если роли изменили в БД, мы считаем обновленные данные)
          Т.е. вариант загрузки всех ролей в начале работы приложения - не подойдет, т.к. данные в БД могут измениться, а нас останется неактуальные роли
        */

        // присваиваем дефолтную роль новому пользователю
        Role userRole = userService.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new RoleNotFoundException("Default Role USER not found.")); // если в БД нет такой роли - выбрасываем исключение
        user.getRoles().add(userRole); // добавить роль USER для создаваемого пользователя

        //зашифровываем пароль(генерим хэш пароля)
        user.setPassword(encoder.encode(user.getPassword()));

        Activity activity = new Activity();
        activity.setUser(user);
        activity.setUuid(UUID.randomUUID().toString());

        userService.register(user, activity); //сохраняем пользователя в бд

        return ResponseEntity.ok().build(); // 200 Ok
    }


    //метод всем будет доступен для вызова, не защищаем его с помощью токенов
    @PostMapping("/login")
    public ResponseEntity<User> login(@Valid @RequestBody User user) {

        //проверяем логин-пароль
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
                );

        // добавляем в Spring-контейнер информацию об авторизации (чтобы Spring понимал, что пользователь успешно вошел и мог использовать его роли и другие параметры)
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // UserDetailsImpl - спец. объект, который хранится в Spring контейнере и содержит данные пользователя
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // активирован пользователь или нет (проверяем только после того, как пользователь успешно залогинился)
        if (!userDetails.isActivated()) {
            throw new DisabledException("User disabled"); // клиенту отправится ошибка о том, что пользователь не активирован
        }

        // если мы дошли до этой строки, значит пользователь успешно залогинился
        // после каждого успешного входа генерируется новый jwt, чтобы следующие запросы на backend авторизовывать автоматически
        String jwt = jwtUtils.createAccessToken(userDetails.getUser());

        userDetails.getUser().setPassword(null); // пароль нужен только один раз для аутентификации - поэтому можем его занулить, чтобы больше нигде не "засветился"

        // создаем кук со значением jwt (браузер будет отправлять его автоматически на backend при каждом запросе)
        // обратите внимание на флаги безопасности в методе создания кука
        HttpCookie cookie = cookieUtils.createJwtCookie(jwt); // server-side cookie
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString()); // добавляем cookie в заголовок (header)

        // если мы дошли до этой строки, значит пользователь успешно залогинился
        return ResponseEntity.ok().headers(responseHeaders).body(userDetails.getUser());
    }

    //выход из системы, зануляем наш кук с jwt
    @PostMapping("/logout")
    public ResponseEntity logout() {
        HttpCookie cookie = cookieUtils.deleteJwtCookie();

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok().headers(responseHeaders).build();
    }


//    Автологин происходит в фильтре
//    @PostMapping("/auto")
//    public ResponseEntity<User> autoLogin() {
//        return null;
//    }



    // активация пользователя (чтобы мог авторизоваться и работать дальше с приложением)
    @PostMapping("/activate-account")
    public ResponseEntity<Boolean> activateUser(@RequestBody String uuid) { // true - успешно активирован

        // проверяем UUID пользователя, которого хотим активировать
        Activity activity = userService.findActivityByUuid(uuid)
                .orElseThrow(() -> new UsernameNotFoundException("Activity Not Found with uuid: " + uuid));

        // если пользователь уже был ранее активирован
        if (activity.isActivated())
            throw new UserAlreadyActivatedException("User already activated");

        // возвращает кол-во обновленных записей (в нашем случае должна быть 1)
        int updatedCount = userService.activate(uuid); // активируем пользователя

        return ResponseEntity.ok(updatedCount == 1); // 1 - значит запись обновилась успешно, 0 - что-то пошло не так
    }


    /*
   Метод перехватывает все ошибки в контроллере (неверный логин-пароль и пр.)
   Даже без этого метода все ошибки будут отправляться клиенту, просто здесь это можно кастомизировать, например отправить JSON в нужном формате
   Можно настроить, какие типа ошибок отправлять в явном виде, а какие нет (чтобы не давать лишнюю информацию злоумышленникам)
    */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<JsonException> handleException(Exception ex) {

        /*

        DisabledException (наш созданный класс) - не активирован
        UserAlreadyActivatedException - пользователь уже активирован (пытается неск. раз активировать)
        UsernameNotFoundException - username или email не найден в базе

        BadCredentialsException - неверный логин-пароль (или любые другие данные)
        UserOrEmailExistsException - пользователь или email уже существуют
        DataIntegrityViolationException - ошибка уникальности в БД

        Эти типы ошибок можно будет считывать на клиенте и обрабатывать как нужно (например, показать текст ошибки)

*/
        // Spring автоматически конвертирует объект JsonException в JSON
        return new ResponseEntity<>(new JsonException(ex.getClass().getSimpleName()), HttpStatus.BAD_REQUEST);
    }

}
