package com.alexgrig.education.springboottasks.auth.config;

import com.alexgrig.education.springboottasks.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity(debug = true)
public class SpringConfig extends WebSecurityConfigurerAdapter {

    // для получения пользователя из БД
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) { // внедряем наш компонент Spring @Service
        this.userDetailsService = userDetailsService;
    }

    //кодировщик паролей, односторонний алгоритм хэширования BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // используем стандартный готовый authenticationManager из Spring контейнера (используется для проверки логина-пароля)
    // эти методы доступны в документации Spring Security - оттуда их можно копировать, чтобы не писать вручную
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    // указываем наш сервис userDetailsService для проверки пользователя в БД и кодировщик паролей
    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception { // настройки AuthenticationManager для правильной проверки логин-пароль
        authenticationManagerBuilder.
                userDetailsService(userDetailsService). // использовать наш сервис для загрузки User из БД
                passwordEncoder(passwordEncoder()); // указываем, что используется кодировщик пароля (для корректной проверки пароля)
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        //отключаем хранение сессии на сервере, т.к. клиент будет использовать RESTful API сервера
        //и создавать токен с информацией о пользователе
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        
        /*
        Если используется другая клиентская технология (не Spring MVC), то отключаем встроенную Spring-защиту
        от csrf атак
         */
        http.csrf().disable();  //отключаем на этапе разаработки, чтобы не было ошибок доступа(для post, put и др.)

        http.formLogin().disable();  //т.к. форма авторизации создается не на Spring технологии, а на клиентской технологии
        http.httpBasic().disable(); //отключаем стандартную браузерную форму авторизации

        http.requiresChannel().anyRequest().requiresSecure(); //use https
    }
}
