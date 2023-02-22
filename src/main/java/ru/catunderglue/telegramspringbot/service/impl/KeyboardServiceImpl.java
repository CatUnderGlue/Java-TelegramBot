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

        InlineKeyboardButton updateTaskButton = new InlineKeyboardButton();
        updateTaskButton.setText("Изменить существующую задачу");
        updateTaskButton.setCallbackData("/update_task");
        row.add(updateTaskButton);
        keyboardRows.add(row);

        row = new ArrayList<>();

        InlineKeyboardButton setUserTimezoneButton = new InlineKeyboardButton();
        setUserTimezoneButton.setText("Настройка часового пояса");
        setUserTimezoneButton.setCallbackData("/set_user_timezone");
        row.add(setUserTimezoneButton);
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

    @Override
    public InlineKeyboardMarkup getTimezonesKeyboard(){
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton checkTimezoneButton = new InlineKeyboardButton();
        checkTimezoneButton.setText("Проверка часового пояса");
        checkTimezoneButton.setCallbackData("/set_user_timezone check");
        row.add(checkTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton kaliningradTimezoneButton = new InlineKeyboardButton();
        kaliningradTimezoneButton.setText("Калининградское время (-1)");
        kaliningradTimezoneButton.setCallbackData("/set_user_timezone KALININGRAD");
        row.add(kaliningradTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton moscowTimezoneButton = new InlineKeyboardButton();
        moscowTimezoneButton.setText("Московское время");
        moscowTimezoneButton.setCallbackData("/set_user_timezone MOSCOW");
        row.add(moscowTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton samaraTimezoneButton = new InlineKeyboardButton();
        samaraTimezoneButton.setText("Самарское время (+1)");
        samaraTimezoneButton.setCallbackData("/set_user_timezone SAMARA");
        row.add(samaraTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton yekaterinburgTimezoneButton = new InlineKeyboardButton();
        yekaterinburgTimezoneButton.setText("Екатеринбургское время (+2)");
        yekaterinburgTimezoneButton.setCallbackData("/set_user_timezone YEKATERINBURG");
        row.add(yekaterinburgTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton omskTimezoneButton = new InlineKeyboardButton();
        omskTimezoneButton.setText("Омское время (+3)");
        omskTimezoneButton.setCallbackData("/set_user_timezone OMSK");
        row.add(omskTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton krasnoyarskTimezoneButton = new InlineKeyboardButton();
        krasnoyarskTimezoneButton.setText("Красноярское время (+4)");
        krasnoyarskTimezoneButton.setCallbackData("/set_user_timezone KRASNOYARSK");
        row.add(krasnoyarskTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton irkutskTimezoneButton = new InlineKeyboardButton();
        irkutskTimezoneButton.setText("Иркутское время (+5)");
        irkutskTimezoneButton.setCallbackData("/set_user_timezone IRKUTSK");
        row.add(irkutskTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton yakutskTimezoneButton = new InlineKeyboardButton();
        yakutskTimezoneButton.setText("Якутское время (+6)");
        yakutskTimezoneButton.setCallbackData("/set_user_timezone YAKUTSK");
        row.add(yakutskTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton vladivostokTimezoneButton = new InlineKeyboardButton();
        vladivostokTimezoneButton.setText("Владивостокское время (+7)");
        vladivostokTimezoneButton.setCallbackData("/set_user_timezone VLADIVOSTOK");
        row.add(vladivostokTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton srednekolymskTimezoneButton = new InlineKeyboardButton();
        srednekolymskTimezoneButton.setText("Среднеколымское время (+8)");
        srednekolymskTimezoneButton.setCallbackData("/set_user_timezone SREDNEKOLYMSK");
        row.add(srednekolymskTimezoneButton);
        keyboardRows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton kamchatkaTimezoneButton = new InlineKeyboardButton();
        kamchatkaTimezoneButton.setText("Камчатское время (+9)");
        kamchatkaTimezoneButton.setCallbackData("/set_user_timezone KAMCHATSK");
        row.add(kamchatkaTimezoneButton);
        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);
        return replyKeyboardMarkup;
    }
}
