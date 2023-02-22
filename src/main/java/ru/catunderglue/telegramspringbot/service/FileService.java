package ru.catunderglue.telegramspringbot.service;

public interface FileService {

    /**
     * Сохранить задачи в файл
     *
     * @param json - задачи в формате json
     * @return true(сохранились)/false(не сохранились)
     */
    boolean saveTasksToFile(String json);

    /**
     * Чтение задач из файла
     *
     * @return String с задачами из файла
     */
    String readTasksFromFile();

    /**
     * Сохранить уведомления в файл
     *
     * @param json - уведомления в формате json
     * @return true(сохранились)/false(не сохранились)
     */
    boolean saveNotificationsToFile(String json);

    /**
     * Чтение уведомлений из файла
     *
     * @return String с уведомлениями из файла
     */
    String readNotificationsFromFile();
}
