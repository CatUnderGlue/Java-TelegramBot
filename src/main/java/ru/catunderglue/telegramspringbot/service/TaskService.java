package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.Task;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TaskService {

    /**
     * @param title       Название задачи
     * @param description Описание для задачи
     * @param date        Дата выполнения
     * @param time        Время выполнения
     * @param userId      id пользователя
     * @return Созданная задача
     */
    Task create(String title, String description, String date, String time, long userId);

    /**
     * @return Все задачи из бд
     */
    List<Task> readAll();

    /**
     * @param id id задачи
     * @return Задачу по заданному id
     */
    Optional<Task> readOne(int id);

    /**
     * @param task Изменённая задача
     * @param id   id старой задачи
     * @return Успешно прошли изменения или нет
     */
    boolean update(Task task, int id, long userId);

    /**
     * @param id id задачи
     * @return Успешно прошло удаление задачи или нет
     */
    boolean delete(int id, long userId);

    /**
     * @param userId id пользователя
     * @return Список с задачами указанного пользователя
     */
    List<Task> getTasksByUser(int userId);

    /**
     * @return Получение задач на день для всех
     */
    Map<Integer, Map<LocalTime, Task>> getTaskByDayForAll() throws SQLException;

    /**
     * @param userId id пользователя
     * @return Карту с ключём-время и значением-задача на текущий день для конкретного пользователя
     */
    Map<LocalTime, Task> getTasksForToday(int userId);

    /**
     * @param filter Условия очищения задач (lambda)
     */
    void clearTasks(CheckTask filter);
}
