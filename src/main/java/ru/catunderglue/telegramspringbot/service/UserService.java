package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.User;

import java.util.List;

public interface UserService {
    /**
     * @param user Новый пользователь
     */
    void create(User user);

    /**
     * @return Список всех пользователей из бд
     */
    List<User> readAll();

    /**
     * @param id id пользователя
     * @return Пользователь по указанному id
     */
    User read(long id);

    /**
     * @param userId id пользователя
     * @return Находится пользователь в бд или нет
     */
    boolean isContains(int userId);
}
