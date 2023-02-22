package ru.catunderglue.telegramspringbot.service;

public interface NotificationService {
    /**
     * Установка утреннего уведомления
     *
     * @param userID - Id пользователя
     * @param toggle - true(Вкл)/false(Выкл)
     */
    void setMorningNotification(Long userID, Boolean toggle);

    /**
     * Установка уведомления: за 30 минут до начала
     *
     * @param userID - Id пользователя
     * @param toggle - true(Вкл)/false(Выкл)
     */
    void setBeforeTaskNotification(Long userID, Boolean toggle);

    /**
     * Установка всех уведомлений
     *
     * @param userId - Id пользователя
     * @param toggle - true(Вкл)/false(Выкл)
     */
    void setAllNotification(Long userId, Boolean toggle);

    /**
     * Проверка состояния утреннего уведомления
     *
     * @param userID - Id пользователя
     * @return true(Вкл)/false(Выкл)
     */
    boolean checkMorningNotification(Long userID);

    /**
     * Проверка состояния уведомления: за 30 минут до начала
     *
     * @param userID - Id пользователя
     * @return true(Вкл)/false(Выкл)
     */
    boolean checkBeforeTaskNotification(Long userID);

    /**
     * Наличие пользователя в карте с уведомлениями
     *
     * @param userId - Id пользователя
     * @return true(есть)/false(нет)
     */
    boolean isContains(Long userId);
}
