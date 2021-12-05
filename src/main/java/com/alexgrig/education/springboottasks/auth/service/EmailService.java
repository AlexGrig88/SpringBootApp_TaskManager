package com.alexgrig.education.springboottasks.auth.service;

import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.Future;


/*
Методы отправки письма желательно выполнять в параллельном потоке, чтобы не задерживать пользователя.
Самый простой способ:
- @EnableAsync - разрешает асинхронный вызов методов (прописать в конфиге Spring)
- @Async - запускает метод в параллельном потоке
- если метод возвращает какой-либо тип, его нужно обернуть в спец. объект AsyncResult

Примеры, документация:
https://spring.io/guides/gs/async-method/

Сервис может вызываться из любых Spring компонентов, в том числе из контроллеров
 */

@Service
@Log
public class EmailService {

    @Value("${client.url}")
    private String clientURL;

    @Value("${email.from}")
    private String emailFrom;

    private JavaMailSender sender;

    @Autowired
    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async
    public Future<Boolean> sendActivationEmail(String email, String username, String uuid) {
        try {
            MimeMessage mimeMessage = sender.createMimeMessage(); // создаем не обычный текстовый документ, а HTML
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8");

            String url = clientURL + "/activate-account/" + uuid; //ссылка на клиенте для активации аккаунта

            String htmlMessage = String.format("Здравствуйте уважаемый %s.<br/><br/>" +
                    "Вы создали аккаунт для приложения \"Менеджер задач\".<br/><br/>" +
                    "<a href='%s'>%s<a/><br/><br/>", username, url, "Для подверждения регистрации нажмите на ссылку");
            mimeMessage.setContent(htmlMessage, "text/html");

            message.setTo(email);
            message.setFrom(emailFrom);
            message.setSubject("Требуется активация аккаунта");
            message.setText(htmlMessage, true);

            sender.send(mimeMessage);

            return new AsyncResult<>(true);

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return new AsyncResult<>(false);
    }

    // Мы в письме не можем передать token с помощью кука, поэтому прикрепляем его к URL как get-параметр.
    // Клиент при нажатии на ссылку из письма - получит этот токен (для последующей авторизации запроса на сервер)
    @Async
    public Future<Boolean> sendResetPasswordEmail(String email, String token) { // т.к. метод выполняется в паралл. потоке, то возвращать должны спец. объект Future<нужный тип>
        try {
            MimeMessage mimeMessage = sender.createMimeMessage(); // создаем не обычный текстовый документ, а HTML
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, "utf-8"); // контейнер для отправки письма

            String url = clientURL + "/update-password/" + token; // ссылка на клиенте, после перехода на которую пользователь сможет ввести новый пароль
            // эту ссылку клиент должен обработать и послать соотв. запрос на backend


            // текст письма в формате HTML
            String htmlMsg = String.format(
                    "Здравствуйте.<br/><br/>" +
                            "Кто-то запросил сброс пароля для веб приложения \"Планировщик дел\".<br/><br/>" +
                            "Если это были не вы - просто удалите это письмо.<br/><br/> Нажмите на ссылку ниже, если хотите сбросить пароль: <br/><br/> " +
                            "<a href='%s'>%s</a><br/><br/>", url, "Сбросить пароль"); // вместо %s будет подставляться значение в порядке следования

            mimeMessage.setContent(htmlMsg, "text/html"); // тип письма - HTML

            message.setTo(email); // email получателя
            message.setSubject("Сброс пароля"); // тема
            message.setFrom(emailFrom); // обратный адрес
            message.setText(htmlMsg, true); // явно надо указать, что это HTML письмо
            sender.send(mimeMessage); // отправка

            return new AsyncResult<>(true); // оборачиваем результат в спец. объект AsyncResult, чтобы результат вернулся после выполнение параллельного потока

        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return new AsyncResult<>(false); // оборачиваем результат в спец. объект AsyncResult

    }
}
