package com.alexgrig.education.springboottasks.auth.config;

import com.alexgrig.education.springboottasks.auth.filter.AuthTokenFilter;
import com.alexgrig.education.springboottasks.auth.filter.ExceptionHandlerFilter;
import com.alexgrig.education.springboottasks.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.SessionManagementFilter;

@Configuration
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled = true) //можно использовать аннотации pre/post в компонентах Spring
@EnableAsync
public class SpringConfig extends WebSecurityConfigurerAdapter {

    // для получения пользователя из БД
    private UserDetailsServiceImpl userDetailsService;
    // перехватывает все выходящие запросы (проверяет jwt если необходимо, автоматически логинит пользователя)
    private AuthTokenFilter authTokenFilter; // его нужно зарегистрировать в filterchain
    private ExceptionHandlerFilter exceptionHandlerFilter;

    @Autowired
    public void setUserDetailsService(UserDetailsServiceImpl userDetailsService) { // внедряем наш компонент Spring @Service
        this.userDetailsService = userDetailsService;
    }

    @Autowired
    public void setAuthTokenFilter(AuthTokenFilter authTokenFilter) { // внедряем фильтр
        this.authTokenFilter = authTokenFilter;
    }

    @Autowired
    public void setExceptionHandlerFilter(ExceptionHandlerFilter exceptionHandlerFilter) {
        this.exceptionHandlerFilter = exceptionHandlerFilter;
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

    // нужно отключить вызов фильтра AuthTokenFilter для сервлет контейнера (чтобы фильтр вызывался не 2 раза, а только один раз из Spring контейнера)
    // https://stackoverflow.com/questions/39314176/filter-invoke-twice-when-register-as-spring-bean
    @Bean
    public FilterRegistrationBean registration(AuthTokenFilter filter) {
        FilterRegistrationBean registration = new FilterRegistrationBean(filter); // FilterRegistrationBean - регистратор фильтров для сервлет контейнера
        registration.setEnabled(false); // отключить исп-е фильтра для сервлет контейнера
        return registration;
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

        // authTokenFilter - валидация JWT, до того, как запрос попадет в контроллер
        http.addFilterBefore(authTokenFilter, SessionManagementFilter.class); // добавляем наш фильтр в securityfilterchain

        //отправляет ошибки последующих фильтров
        http.addFilterBefore(exceptionHandlerFilter, AuthTokenFilter.class);
    }
}
