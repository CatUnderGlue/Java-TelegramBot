package ru.catunderglue.telegramspringbot.service.impl;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.catunderglue.telegramspringbot.service.KeyboardService;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeyboardServiceImpl implements KeyboardService {

    @Override
    public InlineKeyboardMarkup getMenuKeyboard(){
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton createTaskButton = new InlineKeyboardButton();
        createTaskButton.setText("Создать задачу");
        createTaskButton.setCallbackData("/create_task");
        row.add(createTaskButton);

        InlineKeyboardButton todayTasksButton = new InlineKeyboardButton();
        todayTasksButton.setText("Задачи на сегодня");
        todayTasksButton.setCallbackData("/get_tasks_for_today");
        row.add(todayTasksButton);

        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton getTaskButton = new InlineKeyboardButton();
        getTaskButton.setText("Получить все задачи");
        getTaskButton.setCallbackData("/get_tasks");
        row.add(getTaskButton);

        InlineKeyboardButton deleteTaskButton = new InlineKeyboardButton();
        deleteTaskButton.setText("Удалить задачу по id");
        deleteTaskButton.setCallbackData("/delete_task");
        row.add(deleteTaskButton);

        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton setMorningNotificationButton = new InlineKeyboardButton();
        setMorningNotificationButton.setText("Вкл/выкл утреннего уведомления");
        setMorningNotificationButton.setCallbackData("/set_morning_notification");
        row.add(setMorningNotificationButton);
        keyboardRows.add(row);

        row = new ArrayList<>();

        InlineKeyboardButton setBeforeTaskNotificationButton = new InlineKeyboardButton();
        setBeforeTaskNotificationButton.setText("Вкл/выкл уведомления за 30 минут до начала");
        setBeforeTaskNotificationButton.setCallbackData("/set_before_task_notification");
        row.add(setBeforeTaskNotificationButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }

    @Override
    public InlineKeyboardMarkup getMorningNotificationKeyboard(){
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton toggleNotificationOnButton = new InlineKeyboardButton();
        toggleNotificationOnButton.setText("Включить");
        toggleNotificationOnButton.setCallbackData("/set_morning_notification 1");
        row.add(toggleNotificationOnButton);

        InlineKeyboardButton toggleNotificationOffButton = new InlineKeyboardButton();
        toggleNotificationOffButton.setText("Выключить");
        toggleNotificationOffButton.setCallbackData("/set_morning_notification 0");
        row.add(toggleNotificationOffButton);

        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }

    @Override
    public InlineKeyboardMarkup getBeforeTaskNotificationKeyboard(){
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton toggleNotificationOnButton = new InlineKeyboardButton();
        toggleNotificationOnButton.setText("Включить");
        toggleNotificationOnButton.setCallbackData("/set_before_task_notification 1");
        row.add(toggleNotificationOnButton);

        InlineKeyboardButton toggleNotificationOffButton = new InlineKeyboardButton();
        toggleNotificationOffButton.setText("Выключить");
        toggleNotificationOffButton.setCallbackData("/set_before_task_notification 0");
        row.add(toggleNotificationOffButton);

        keyboardRows.add(row);
        replyKeyboardMarkup.setKeyboard(keyboardRows);

        return replyKeyboardMarkup;
    }
}
