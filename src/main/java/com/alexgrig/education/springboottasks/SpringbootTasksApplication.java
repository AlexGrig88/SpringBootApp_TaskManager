package com.alexgrig.education.springboottasks;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class SpringbootTasksApplication {
    @Value("${client.url}")
    private String clientURL;


    public static void main(String[] args) {
        SpringApplication.run(SpringbootTasksApplication.class, args);
    }

    /*
С помощью WebMvcConfigurer можно задать глобальные правила cors сразу для всех контроллеров/методов
Если необходимо в каждом контроллере можно задавать свои настройки
*/
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.
                        addMapping("/**"). // для всех URL
                        allowedOrigins(clientURL). // с каких адресов разрешать запросы (можно указывать через запятую)
                        allowCredentials(true). // разрешить отправлять куки для межсайтового запроса
                        allowedHeaders("*"). // разрешить все заголовки - без этой настройки в некоторых браузерах может не работать
                        allowedMethods("*"); // все методы разрешены (GET,POST и пр.) - без этой настройки CORS не будет работать!
            }
        };
    }

}
