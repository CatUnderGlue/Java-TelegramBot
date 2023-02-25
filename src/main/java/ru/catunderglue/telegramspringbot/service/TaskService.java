package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.Task;

import java.time.LocalTime;
import java.util.Map;
import java.util.Set;

public interface TaskService {

    /**
     * Создание новой задачи и добавление её в taskMap
     *
     * @param title Заголовок задачи
     * @param description Описание задачи
     * @param date Дата выполнения
     * @param time Время выполнения
     * @param userId Id пользователя
     * @return Id пользователя
     */
    Long createTask(String title, String description, String date, String time, long userId);

    /**
     * Получение всех задач конкретного пользователя
     *
     * @param userId Id пользователя
     * @return Map(Id задачи, задача)
     */
    Map<Long, Task> getTasks(long userId);

    /**
     * Получение id всех пользователей
     *
     * @return Set(id пользователей)
     */
    Set<Long> getUsersIds();

    /**
     * Получение задачи по её id от конкретного пользователя
     *
     * @param id Id задачи
     * @param userId Id пользователя
     * @return Искомая задача
     */
    Task getTaskById(Long id, long userId);

    /**
     * Обновление уже существующей задачи
     *
     * @param id Id задачи
     * @param task Новая задача
     * @param userId Id пользователя
     * @return Обновлённая задача
     */
    Task updateTask(Long id, Task task, long userId);

    /**
     * Удаление задачи по Id для конкретного пользователя
     *
     * @param id Id задачи
     * @param userId Id пользователя
     * @return Удалённая задача
     */
    Task removeTask(Long id, long userId);

    /**
     * Получение задач на сегодня для всех пользователей
     *
     * @return Карта (Id пользователя, Карта(Время, Задача))
     */
    Map<Long, Map<LocalTime, Task>> getTaskByDayForAll();

    /**
     * Получение задач на сегодня для конкретного пользователя
     *
     * @param userId Id пользователя
     * @return Карта(Время, Задача)
     */
    Map<LocalTime, Task> getTasksForToday(long userId);

    void clearTasks(CheckTask filter);
}
