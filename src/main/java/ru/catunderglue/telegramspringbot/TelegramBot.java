package ru.catunderglue.telegramspringbot;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.catunderglue.telegramspringbot.config.BotConfig;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.model.User;
import ru.catunderglue.telegramspringbot.model.enums.Timezones;
import ru.catunderglue.telegramspringbot.service.KeyboardService;
import ru.catunderglue.telegramspringbot.service.NotificationService;
import ru.catunderglue.telegramspringbot.service.TaskService;
import ru.catunderglue.telegramspringbot.service.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final KeyboardService keyboardService;
    private final UserService userService;

    private static final String separator = "=====================================";

    public TelegramBot(BotConfig config,
                       TaskService taskService,
                       NotificationService notificationService,
                       KeyboardService keyboardService, UserService userService) {
        this.config = config;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.keyboardService = keyboardService;
        this.userService = userService;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().strip().split("\s")[0];
            long chatId = update.getMessage().getChatId();
            menu(messageText, chatId, update);
        } else if (update.hasCallbackQuery()) {
            String callbackQueryText = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            menu(callbackQueryText, chatId, update);
        }

    }

    // /menu
    private void menuCommandReceived(int chatId, Update update) {
        SendMessage message = buildMessage(chatId, "Welcome, " + update.getMessage().getChat().getFirstName() + " ^-^\n");
        message.setReplyMarkup(keyboardService.getMenuKeyboard());
        sendMessage(message);
        // Инициализирует базы данных, если пользователь первый раз использует бота
        if (!userService.isContains(chatId)){
            userService.create(new User(chatId, update.getMessage().getChat().getFirstName()));
        }
        if (!notificationService.isContains(chatId)) {
            notificationService.setAllNotification(chatId, true);
        }
        if (!notificationService.isTimezoneContains(chatId)) {
            notificationService.setUserTimezone(chatId, Timezones.MOSCOW);
        }
    }

    // /id
    private void idCommandReceived(int chatId, long userId) {
        String answer = "Your id: " + userId;
        buildMessage(chatId, answer);
    }

    // ================================================================================================================
    // Tasks
    // /create_task
    private void createTaskCommandReceived(int chatId, Update update) {
        String timezone = notificationService.getUserTimezone(chatId);
        LocalDate now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalDate();
        if (update.getMessage() == null || update.getMessage().getText().equals("/create_task")) {
            sendMessage(buildMessage(chatId, "Чтобы создать задачу, введите эту команду в таком формате:\nСимволы \"|\" обязательны."));
            sendMessage(buildMessage(chatId, String.format("```\n/create_task название|описание|%s|00:00\n```", now)));
        } else {
            try {
                String[] messageParts = update.getMessage().getText().split("\s", 2);
                String[] taskParts = messageParts[1].split("\\|");
                taskService.create(taskParts[0], taskParts[1], taskParts[2], taskParts[3], chatId);
                sendMessage(buildMessage(chatId, "Задача успешно создана!"));
            } catch (IndexOutOfBoundsException e) {
                sendMessage(buildMessage(chatId, "Неверный формат времени."));
            } catch (Exception e) {
                sendMessage(buildMessage(chatId, e.getMessage()));
            }
        }
    }

    // /get_tasks
    private void getTasksCommandReceived(int chatId) {
        StringBuilder builder = new StringBuilder();
        List<Task> tasks = taskService.getTasksByUser(chatId);
        if (tasks == null || tasks.size() == 0) {
            sendMessage(buildMessage(chatId, "У вас пока что нет задач :c"));
            return;
        }
        for (Task task : tasks) {
            builder.append("id: ").append(task.getId()).append("\n").append(task);
        }
        sendMessage(buildMessage(chatId, builder.toString()));
    }

    private void updateTaskCommandReceived(int chatId, Update update) {
        String timezone = notificationService.getUserTimezone(chatId);
        LocalDate now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalDate();
        if (update.getMessage() == null || update.getMessage().getText().equals("/update_task")) {
            sendMessage(buildMessage(chatId, "Чтобы изменить задачу, введите эту команду в таком формате:\nСимволы \"|\" обязательны."));
            sendMessage(buildMessage(chatId, String.format("```\n/update_task id название|описание|%s|00:00\n```", now)));
        } else {
            try {
                String[] messageParts = update.getMessage().getText().split("\s", 3);
                int id = Integer.parseInt(messageParts[1]);
                String[] taskParts = messageParts[2].split("\\|");
                LocalDate parsedDate = LocalDate.parse(taskParts[2]);
                LocalTime parsedTime = LocalTime.parse(taskParts[3]);
                Task updateTask = new Task(chatId, taskParts[0], taskParts[1], parsedDate, parsedTime);
                taskService.update(updateTask, id);
                sendMessage(buildMessage(chatId, String.format("Задача под номером %d успешно обновлена!", id)));
            } catch (Exception e) {
                sendMessage(buildMessage(chatId, "Ошибка в обновлении задачи, придерживайтесь указанного формата."));
            }
        }
    }

    // /delete_task
    private void deleteTaskCommandReceived(long chatId, Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().strip().equals("/delete_task")) {
            sendMessage(buildMessage(chatId, "Чтобы удалить нужную вам задачу, введите эту команду в таком формате:"));
            sendMessage(buildMessage(chatId,
                    """
                            ```
                            /delete_task id
                            ```
                            """));
            return;
        }
        String[] messageParts = update.getMessage().getText().split("\s", 2);
        if (taskService.delete(Integer.parseInt(messageParts[1]))) {
            sendMessage(buildMessage(chatId, "Задача под номером " + Long.valueOf(messageParts[1]) + " успешно удалена."));
            return;
        }
        sendMessage(buildMessage(chatId, "Задача под данным id не найдена."));

    }

    // /get_tasks_for_today
    private void getTasksForTodayCommandReceived(int chatId) {
        Map<LocalTime, Task> tasks = taskService.getTasksForToday(chatId);
        String timezone = notificationService.getUserTimezone(chatId);
        if (!tasks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Ваши задачи на ").append(ZonedDateTime.now(ZoneId.of(timezone)).toLocalDate()).append(": \n").append(separator).append("\n");
            for (Task task : tasks.values()) {
                sb.append("Id: ").append(task.getId()).append("\n")
                        .append(task)
                        .append(separator).append("\n");
            }
            sendMessage(buildMessage(chatId, sb.toString()));
        } else {
            sendMessage(buildMessage(chatId, "Список задач на сегодня пуст"));
        }
    }

    @Scheduled(cron = "0 * * * * *")
    private void clearOldTask() {
        taskService.clearTasks((Task task) -> task.getDate().plusDays(1).isBefore(LocalDate.now()));
    }

    // ================================================================================================================
    // Notifications

    @Scheduled(cron = "0 0 0 * * *")
    private void sendAllUsersTasksForToday() {
        try {
            Map<Integer, Map<LocalTime, Task>> tasksByDay = taskService.getTaskByDayForAll();
            for (Map.Entry<Integer, Map<LocalTime, Task>> entry : tasksByDay.entrySet()) {
                String timezone = notificationService.getUserTimezone(entry.getKey());
                if (ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime().getHour() == 6) {
                    if (!entry.getValue().isEmpty()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Ваши задачи на ")
                                .append(LocalDate.now())
                                .append(": \n")
                                .append(separator)
                                .append("\n");
                        for (Map.Entry<LocalTime, Task> taskEntry : entry.getValue().entrySet()) {
                            sb.append("Id: ")
                                    .append(taskEntry.getValue().getId())
                                    .append("\n").append(taskEntry.getValue())
                                    .append(separator)
                                    .append("\n");
                        }
                        sendMessage(buildMessage(entry.getKey(), sb.toString()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 * * * * *")
    private void sendAllUsersTaskNotification() {
        try {
            Map<Integer, Map<LocalTime, Task>> tasksByDay = taskService.getTaskByDayForAll();
            for (Map.Entry<Integer, Map<LocalTime, Task>> entry : tasksByDay.entrySet()) {
                String timezone = notificationService.getUserTimezone(entry.getKey());
                LocalTime now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime();
                if (!notificationService.isContains(entry.getKey())) {
                    notificationService.setAllNotification(entry.getKey(), true);
                }
                boolean flag = false;
                if (notificationService.checkBeforeTaskNotification(entry.getKey()) && !entry.getValue().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Напоминаю, что через 30 минут у вас есть запланированная задача: \n")
                            .append(separator)
                            .append("\n");
                    for (Map.Entry<LocalTime, Task> taskEntry : entry.getValue().entrySet()) {
                        if (taskEntry.getKey().minusMinutes(30).getHour() == now.getHour() && taskEntry.getKey().minusMinutes(30).getMinute() == now.getMinute()) {
                            sb.append("Id: ")
                                    .append(taskEntry.getValue().getId())
                                    .append("\n").append(taskEntry.getValue())
                                    .append(separator)
                                    .append("\n");
                            flag = true;
                        }
                    }
                    if (flag) {
                        sendMessage(buildMessage(entry.getKey(), sb.toString()));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setMorningNotificationCommandReceived(int chatId, Update update) {
        if (update.getCallbackQuery().getData().strip().equals("/set_morning_notification")) {
            SendMessage message = buildMessage(chatId, "Настройка утреннего уведомления");
            message.setReplyMarkup(keyboardService.getMorningNotificationKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        boolean flag = switch (messageParts[1]) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("0 - выкл, 1 - вкл");
        };

        try {
            notificationService.setMorningNotification(chatId, flag);
        } catch (IllegalArgumentException e) {
            sendMessage(buildMessage(chatId, e.getMessage()));
            return;
        }

        sendMessage(buildMessage(chatId, "Утреннее уведомление " + (flag ? "включено." : "выключено.")));
    }

    private void setBeforeTaskNotificationCommandReceived(int chatId, Update update) {
        if (update.getCallbackQuery().getData().strip().equals("/set_before_task_notification")) {
            SendMessage message = buildMessage(chatId, "Настройка уведомления: за 30 минут до начала");
            message.setReplyMarkup(keyboardService.getBeforeTaskNotificationKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        boolean flag = switch (messageParts[1]) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("0 - выкл, 1 - вкл");
        };

        try {
            notificationService.setBeforeTaskNotification(chatId, flag);
        } catch (IllegalArgumentException e) {
            sendMessage(buildMessage(chatId, e.getMessage()));
            return;
        }

        sendMessage(buildMessage(chatId, "Уведомление: за 30 минут до начала " + (flag ? "включено." : "выключено.")));
    }

    private void setUserTimezoneCommandReceived(int chatId, Update update) {
        if (update.getCallbackQuery().getData().strip().equals("/set_user_timezone")) {
            SendMessage message = buildMessage(chatId, "Настройка вашего часового пояса.");
            message.setReplyMarkup(keyboardService.getTimezonesKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        if (messageParts[1].equals("check")) {
            String timezone = notificationService.getUserTimezone(chatId);
            LocalTime now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime();
            sendMessage(buildMessage(chatId, "Ваше время: " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + "\nВаш часовой пояс: " + timezone));
            return;
        }
        Timezones timezones = switch (messageParts[1]) {
            case "KALININGRAD" -> Timezones.KALININGRAD;
            case "MOSCOW" -> Timezones.MOSCOW;
            case "SAMARA" -> Timezones.SAMARA;
            case "YEKATERINBURG" -> Timezones.YEKATERINBURG;
            case "OMSK" -> Timezones.OMSK;
            case "KRASNOYARSK" -> Timezones.KRASNOYARSK;
            case "IRKUTSK" -> Timezones.IRKUTSK;
            case "YAKUTSK" -> Timezones.YAKUTSK;
            case "VLADIVOSTOK" -> Timezones.VLADIVOSTOK;
            case "SREDNEKOLYMSK" -> Timezones.SREDNEKOLYMSK;
            case "KAMCHATSK" -> Timezones.KAMCHATSK;
            default -> Timezones.MOSCOW;
        };

        notificationService.setUserTimezone(chatId, timezones);

        sendMessage(buildMessage(chatId, "Ваш часовой пояс: " + timezones));
    }

    private void notificationsCommandReceived(int chatId) {
        SendMessage message = buildMessage(chatId, "Настройка уведомлений");
        message.setReplyMarkup(keyboardService.getNotificationsKeyboard());
        sendMessage(message);

    }

    // ================================================================================================================
    // 
    private String userInfo(Update update) {
        return String.format("First name: %s Id: %d About: %s BIO: %s",
                update.getMessage().getChat().getFirstName(), // Имя
                update.getMessage().getChat().getId(), // Id
                update.getMessage().getChat().getDescription() == null ? "-" : update.getMessage().getChat().getDescription(), // Описание
                update.getMessage().getChat().getBio() == null ? "-" : update.getMessage().getChat().getBio()); // БИО
    }

    private SendMessage buildMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        message.enableMarkdown(true);
        return message;
    }

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
        }
    }

    private void menu(String command, Long chatId, Update update) {
        command = command.strip().split("\s", 2)[0];
        int id = Math.toIntExact(chatId);
        switch (command) {
            case "/id" -> idCommandReceived(id, update.getMessage().getChat().getId());
            case "/menu", "/start" -> menuCommandReceived(id, update);
            case "/create_task" -> createTaskCommandReceived(id, update);
            case "/get_tasks" -> getTasksCommandReceived(id);
            case "/delete_task" -> deleteTaskCommandReceived(id, update);
            case "/get_tasks_for_today" -> getTasksForTodayCommandReceived(id);
            case "/set_morning_notification" -> setMorningNotificationCommandReceived(id, update);
            case "/set_before_task_notification" -> setBeforeTaskNotificationCommandReceived(id, update);
            case "/set_user_timezone" -> setUserTimezoneCommandReceived(id, update);
            case "/update_task" -> updateTaskCommandReceived(id, update);
            case "/notifications" -> notificationsCommandReceived(id);
            default -> buildMessage(chatId, "Command was not recognize");
        }
    }

}
