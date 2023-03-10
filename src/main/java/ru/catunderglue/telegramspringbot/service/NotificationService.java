package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.enums.Timezones;

public interface NotificationService {
    /**
     * @param userId id пользователя
     * @param toggle включён/выключен
     * @return успешно прошли изменения или нет
     */
    boolean setMorningNotification(Integer userId, Boolean toggle);

    /**
     * @param userId id пользователя
     * @param toggle включён/выключен
     * @return успешно прошли изменения или нет
     */
    boolean setBeforeTaskNotification(Integer userId, Boolean toggle);

    /**
     * @param userId id пользователя
     * @param toggle включены/выключены
     */
    void setAllNotification(Integer userId, Boolean toggle);

    /**
     * @param userId    id пользователя
     * @param timezones часовой пояс
     */
    void setUserTimezone(Integer userId, Timezones timezones);

    /**
     * @param userId id пользователя
     * @return часовой пояс в формате "Asia/Yekaterinburg"
     */
    String getUserTimezone(Integer userId);

    /**
     * @param userId id пользователя
     * @return включены или выключены утренние уведомления
     */
    boolean checkMorningNotification(Integer userId);

    /**
     * @param userId id пользователя
     * @return включены или выключены уведомления перед началом задачи
     */
    boolean checkBeforeTaskNotification(Integer userId);

    /**
     * @param userId id пользователя
     * @return есть пользователь в таблице с уведомлениями или нет
     */
    boolean isContains(Integer userId);

    /**
     * @param userId id пользователя
     * @return есть пользователь в таблице с часовыми поясами или нет
     */
    boolean isTimezoneContains(Integer userId);
}
