package com.alexgrig.education.springboottasks.business.controller;


import com.alexgrig.education.springboottasks.business.entity.Stat;
import com.alexgrig.education.springboottasks.business.service.StatService;
import com.alexgrig.education.springboottasks.business.util.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatController {

    private final StatService statService; // сервис для доступа к данным (напрямую к репозиториям не обращаемся)

    // автоматическое внедрение экземпляра класса через конструктор
    @Autowired
    public StatController(StatService statService) {
        this.statService = statService;
    }

    // для статистики всегда получаем только одну строку для конкретного пользователя
    @PostMapping("/stat")
    public ResponseEntity<Stat> findByEmail(@RequestBody String email) {

        MyLogger.debugMethodName("StatController: findById()");

        return ResponseEntity.ok(statService.findStat(email));
    }
}
