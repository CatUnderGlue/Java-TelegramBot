package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.Task;

public interface ValidationService {

    /**
     * Проверка даты в задаче на совпадение с текущей
     *
     * @param task   Задача пользователя
     * @return Совпала(true)/Не совпала(false)
     */
    boolean checkDateMatch(int userId ,Task task);
}
