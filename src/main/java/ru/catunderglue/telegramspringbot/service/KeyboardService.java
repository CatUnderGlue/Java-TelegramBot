package ru.catunderglue.telegramspringbot.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

public interface KeyboardService {

    /**
     * Создание клавиатуры для меню
     *
     * @return Клавиатура меню
     */
    InlineKeyboardMarkup getMenuKeyboard();

    /**
     * Создание клавиатуры для утреннего уведомления
     *
     * @return InlineKeyboardMarkup утреннего уведомления
     */
    InlineKeyboardMarkup getMorningNotificationKeyboard();

    /**
     * Создание клавиатуры для уведомления: за 30 минут до начала
     *
     * @return InlineKeyboardMarkup уведомления: за 30 минут до начала
     */
    InlineKeyboardMarkup getBeforeTaskNotificationKeyboard();
}
