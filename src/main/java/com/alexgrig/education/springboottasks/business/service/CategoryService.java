package com.alexgrig.education.springboottasks.business.service;

import com.alexgrig.education.springboottasks.business.entity.Category;
import com.alexgrig.education.springboottasks.business.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    //Methods
    ////////////////////////////////---1---/////////////////////////////////////////
    public List<Category> findAll(String email) {
        return categoryRepository.findByUserEmailOrderByTitleAsc(email);
    }


    public Category addOrUpdate(Category category) {
        return categoryRepository.save(category); //метод save обновляет или создает новый объект, если его не было
    }


    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }


    public List<Category> find(String title, String email) {
        return categoryRepository.find(title, email);
    }


    public Category findById(Long id) {
        return categoryRepository.findById(id).get();  // возвращается Optional
    }
}
