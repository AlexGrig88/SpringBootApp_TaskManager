package com.alexgrig.education.springboottasks.business.service;


import com.alexgrig.education.springboottasks.business.entity.Stat;
import com.alexgrig.education.springboottasks.business.repository.StatRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class StatService {

    private final StatRepository repository; // сервис имеет право обращаться к репозиторию (БД)

    public StatService(StatRepository repository) {
        this.repository = repository;
    }

    public Stat findStat(String email) {
        return repository.findByUserEmail(email);

    }
}