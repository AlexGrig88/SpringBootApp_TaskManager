package com.alexgrig.education.springboottasks.business.controller;

import com.alexgrig.education.springboottasks.business.entity.Category;
import com.alexgrig.education.springboottasks.business.search.CategorySearchValues;
import com.alexgrig.education.springboottasks.business.service.CategoryService;
import com.alexgrig.education.springboottasks.business.util.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

/*
Чтобы дать меньше шансов для взлома (например, CSRF атак): POST/PUT запросы могут изменять/фильтровать закрытые данные,
а GET запросы - для получения незащищенных данных
Т.е. GET-запросы не должны использоваться для изменения/получения секретных данных

Если возникнет exception - вернется код  500 Internal Server Error, поэтому не нужно все действия оборачивать в try-catch

Используем @RestController вместо обычного @Controller, чтобы все ответы сразу оборачивались в JSON,
иначе пришлось бы добавлять лишние объекты в код, использовать @ResponseBody для ответа, указывать тип отправки JSON

Названия методов могут быть любыми, главное не дублировать их имена и URL mapping

*/

@RestController
@RequestMapping("category") // базовый URI
public class CategoryController {

    private CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    //Methods
    ////////////////////////////////---1---/////////////////////////////////////////
    @PostMapping("/all")
    public List<Category> findAll(@RequestBody String email) {

        MyLogger.debugMethodName("CategoryController: findAll(email)");

        return categoryService.findAll(email);
    }


    // для добавления нового объекта используем тип запроса PUT, позволяет передавать значение в body
    @PutMapping("/add")
    public ResponseEntity<Category> add(@RequestBody Category category){ // в category передается объект для вставки в БД

        MyLogger.debugMethodName("CategoryController: add(category)");

        // проверка на обязательные параметры - id НЕ должен быть заполнен, т.к. это добавление нового объекта
        if (category.getId() != null && category.getId() != 0) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title (обязательно должен быть заполнен)
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        // получаем созданный в БД объект уже с ID и передаем его клиенту обратно

//        return new ResponseEntity(categoryService.addOrUpdate(category), HttpStatus.OK);
        return ResponseEntity.ok(categoryService.addOrUpdate(category));

    }


    @PatchMapping("/update")
    public ResponseEntity<Category> update(@RequestBody Category category) {

        MyLogger.debugMethodName("CategoryController: update(category)");

        // проверка на обязательные параметры - id должен быть заполнен, т.к. это обновление существующего объекта
        if (category.getId() == null || category.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title (обязательно должен быть заполнен)
        if (category.getTitle() == null || category.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        categoryService.addOrUpdate(category);
        return new ResponseEntity(HttpStatus.OK);
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {

        MyLogger.debugMethodName("CategoryController: delete(id)");

        if (id == null || id == 0) {
            return new ResponseEntity("not correct param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        try {
            categoryService.delete(id);
        } catch (EmptyResultDataAccessException ex) {
            ex.printStackTrace();
            return new ResponseEntity("id = " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity(HttpStatus.OK); //возвращаем статус 200 без объекта
    }


    @PostMapping("/search")
    public ResponseEntity<List<Category>> search(@RequestBody CategorySearchValues categorySearchValues) {

        MyLogger.debugMethodName("CategoryController: search(categorySearchValues)");

        List<Category> list = categoryService.find(
                categorySearchValues.getTitle(),
                categorySearchValues.getEmail()
        );

        return ResponseEntity.ok(list);
    }


    @PostMapping("/id")
    public ResponseEntity<Category> findById(@RequestBody Long id) {

        MyLogger.debugMethodName("CategoryController: find(id)");

        Category category = null;
        try {
            category = categoryService.findById(id);

        } catch (NoSuchElementException ex) {
            ex.printStackTrace();
            return new ResponseEntity("id = " + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(category);
    }

}
