package com.alexgrig.education.springboottasks.business.controller;

import com.alexgrig.education.springboottasks.business.entity.Task;
import com.alexgrig.education.springboottasks.business.search.TaskSearchValues;
import com.alexgrig.education.springboottasks.business.service.TaskService;
import com.alexgrig.education.springboottasks.business.util.MyLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

/*
В контроллере можно задать свои настройки CORS, отличные от глобальных (из класса SpringConfig)
@CrossOrigin(origins = "https://localhost:4200", allowCredentials = "true")
 */
@RestController
@RequestMapping("/task")
public class TaskController {

    public static final String ID_COLUMN = "id"; // имя столбца id (используется для сортировки и везде, где нужно указать название поля)

    private final TaskService taskService; // сервис для доступа к данным (напрямую к репозиториям не обращаемся)

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // получение всех данных
    @PostMapping("/all")
    public ResponseEntity<List<Task>> findAll(@RequestBody String email) {

        MyLogger.debugMethodName("task: findAll()");

        return ResponseEntity.ok(taskService.findAll(email)); // поиск всех задач конкретного пользователя
    }

    // добавление задачи
    @PutMapping("/add")
    public ResponseEntity<Task> add(@RequestBody Task task) {

        MyLogger.debugMethodName("task: add()");

        // проверка на обязательные параметры
        if (task.getId() != null && task.getId() != 0) {
            // id создается автоматически в БД (autoincrement), поэтому его передавать не нужно, иначе может быть конфликт уникальности значения
            return new ResponseEntity("redundant param: id MUST be null", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (task.getTitle() == null || task.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(taskService.add(task)); // возвращаем созданный объект со сгенерированным id

    }

    // обновление
    @PatchMapping("/update")
    public ResponseEntity<Task> update(@RequestBody Task task) {

        MyLogger.debugMethodName("task: update()");

        // проверка на обязательные параметры
        if (task.getId() == null || task.getId() == 0) {
            return new ResponseEntity("missed param: id", HttpStatus.NOT_ACCEPTABLE);
        }

        // если передали пустое значение title
        if (task.getTitle() == null || task.getTitle().trim().length() == 0) {
            return new ResponseEntity("missed param: title", HttpStatus.NOT_ACCEPTABLE);
        }


        // save работает как на добавление, так и на обновление
        taskService.update(task);

        return new ResponseEntity(HttpStatus.OK); // просто отправляем статус 200 (операция прошла успешно)

    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity delete(@PathVariable("id") Long id) {

        MyLogger.debugMethodName("task: delete() ");

        // можно обойтись и без try-catch, тогда будет возвращаться полная ошибка (stacktrace)
        // здесь показан пример, как можно обрабатывать исключение и отправлять свой текст/статус
        try {
            taskService.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }
        return new ResponseEntity(HttpStatus.OK);
    }

    // получение объекта по id
    @PostMapping("/id")
    public ResponseEntity<Task> findById(@RequestBody Long id) {

        MyLogger.debugMethodName("task: findById()");

        Task task = null;

        try {
            task = taskService.findById(id);
        } catch (NoSuchElementException e) { // если объект не будет найден
            e.printStackTrace();
            return new ResponseEntity("id=" + id + " not found", HttpStatus.NOT_ACCEPTABLE);
        }

        return ResponseEntity.ok(task);
    }

    // поиск по любым параметрам TaskSearchValues
    @PostMapping("/search")
    public ResponseEntity<Page<Task>> search(@RequestBody TaskSearchValues taskSearchValues)  {

        MyLogger.debugMethodName("task: search()");

        // исключить NullPointerException
        String title = taskSearchValues.getTitle() != null ? taskSearchValues.getTitle() : null;

        // конвертируем Boolean в Integer
        Integer completed = taskSearchValues.getCompleted() != null ? taskSearchValues.getCompleted() : null;

        Long priorityId = taskSearchValues.getPriorityId() != null ? taskSearchValues.getPriorityId() : null;
        Long categoryId = taskSearchValues.getCategoryId() != null ? taskSearchValues.getCategoryId() : null;

        String sortColumn = taskSearchValues.getSortColumn() != null ? taskSearchValues.getSortColumn() : null;
        String sortDirection = taskSearchValues.getSortDirection() != null ? taskSearchValues.getSortDirection() : null;

        Integer pageNumber = taskSearchValues.getPageNumber() != null ? taskSearchValues.getPageNumber() : 0;
        Integer pageSize = taskSearchValues.getPageSize() != null ? taskSearchValues.getPageSize() : 10;

        String email = taskSearchValues.getEmail() != null ? taskSearchValues.getEmail() : null; // для показа задач только этого пользователя


        // чтобы захватить в выборке все задачи по датам, независимо от времени - можно выставить время с 00:00 до 23:59
        Date dateFrom = null;
        Date dateTo = null;


        // выставить 00:00 для начальной даты (если она указана)
        if (taskSearchValues.getDateFrom() != null) {

            Calendar calendarFrom = Calendar.getInstance();
            calendarFrom.setTime(taskSearchValues.getDateFrom());
            calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
            calendarFrom.set(Calendar.MINUTE, 0);
            calendarFrom.set(Calendar.SECOND, 0);
            calendarFrom.set(Calendar.MILLISECOND, 0);

            dateFrom = calendarFrom.getTime();

        }

        // выставить 23:59 для конечной даты (если она указана)
        if (taskSearchValues.getDateTo() != null) {

            Calendar calendarTo = Calendar.getInstance();
            calendarTo.setTime(taskSearchValues.getDateTo());
            calendarTo.set(Calendar.HOUR_OF_DAY, 23);
            calendarTo.set(Calendar.MINUTE, 59);
            calendarTo.set(Calendar.SECOND, 59);
            calendarTo.set(Calendar.MILLISECOND, 999);

            dateTo = calendarTo.getTime();

        }

        Sort.Direction direction = sortDirection == null || sortDirection.trim().length() == 0 || sortDirection.trim().equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;

        /* Вторым полем для сортировки добавляем id, чтобы всегда сохранялся строгий порядок.
            Например, если у 2-х задач одинаковое значение приоритета и мы сортируем по этому полю.
            Порядок следования этих 2-х записей после выполнения запроса может каждый раз меняться, т.к. не указано второе поле сортировки.
            Поэтому и используем ID - тогда все записи с одинаковым значением приоритета будут следовать в одном порядке по ID.
         */

        // поле (столбец) сортировки

        if (sortColumn == null){
            sortColumn = ID_COLUMN;
        }

        Sort sort = Sort.by(direction, sortColumn, ID_COLUMN);

        // объект постраничности
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, sort);

        // результат запроса с постраничным выводом
        Page<Task> result = taskService.find(title, completed, priorityId, categoryId, email, dateFrom, dateTo, pageRequest);

        // результат запроса
        return ResponseEntity.ok(result);

    }

}
