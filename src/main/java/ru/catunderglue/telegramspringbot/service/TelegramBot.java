package ru.catunderglue.telegramspringbot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.catunderglue.telegramspringbot.config.BotConfig;
import ru.catunderglue.telegramspringbot.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Slf4j
@Component
@EnableScheduling
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final TaskService taskService;
    private final NotificationService notificationService;
    private final KeyboardService keyboardService;

    private static final String separator = "=====================================";

    public TelegramBot(BotConfig config,
                       TaskService taskService,
                       NotificationService notificationService,
                       KeyboardService keyboardService) {
        this.config = config;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.keyboardService = keyboardService;
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
    private void menuCommandReceived(long chatId, Update update) {
        log.info("/menu command used. " + userInfo(update));
        SendMessage message = buildMessage(chatId, "Welcome, " + update.getMessage().getChat().getFirstName() + " ^-^\n");
        message.setReplyMarkup(keyboardService.getMenuKeyboard());
        sendMessage(message);
        // Включение уведомлений, если пользователь первый раз использует бота
        if (!notificationService.isContains(chatId)) {
            notificationService.setAllNotification(chatId, true);
        }
    }

    // /id
    private void idCommandReceived(long chatId, long userId, Update update) {
        String answer = "Your id: " + userId;
        log.info("/id command used. " + userInfo(update));
        buildMessage(chatId, answer);
    }

    // ================================================================================================================
    // Tasks
    // /create_task
    private void createTaskCommandReceived(long chatId, Update update) {
        if (update.getMessage() == null || update.getMessage().getText().equals("/create_task")) {
            sendMessage(buildMessage(chatId, "Чтобы создать задачу, введите эту команду в таком формате:\nСимволы \"|\" обязательны."));
            sendMessage(buildMessage(chatId, String.format("```\n/create_task название|описание|%s|00:00\n```", LocalDate.now())));
        } else {
            try {
                String[] messageParts = update.getMessage().getText().split("\s", 2);
                String[] taskParts = messageParts[1].split("\\|");
                Long id = taskService.createTask(taskParts[0], taskParts[1], taskParts[2], taskParts[3], chatId);
                sendMessage(buildMessage(chatId, String.format("Задача под номером %d успешно создана!", id)));
                log.info("/create_task command used. " + userInfo(update) + "\nCreated task: " + taskService.getTaskById(id, chatId));
            } catch (Exception e) {
                log.error("/create_task command unsuccessful used. " + userInfo(update));
                sendMessage(buildMessage(chatId, "Ошибка в создании задачи, придерживайтесь указанного формата."));
            }
        }
    }

    // /get_tasks
    private void getTasksCommandReceived(long chatId, Update update) {
        StringBuilder builder = new StringBuilder();
        Map<Long, Task> tasks = taskService.getTasks(chatId);
        if (tasks == null || tasks.size() == 0) {
            sendMessage(buildMessage(chatId, "У вас пока что нет задач :c"));
            return;
        }
        for (Map.Entry<Long, Task> taskEntry : tasks.entrySet()) {
            builder.append("id: ").append(taskEntry.getKey()).append("\n").append(taskEntry.getValue());
        }
        sendMessage(buildMessage(chatId, builder.toString()));
        log.info("/get_tasks command used by " + update.getCallbackQuery().getMessage().getChat().getFirstName());
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
        Task removedTask = taskService.removeTask(Long.valueOf(messageParts[1]), chatId);
        if (removedTask == null) {
            sendMessage(buildMessage(chatId, "Задача под данным id не найдена."));
            return;
        }
        log.info("/delete_task command used by " + update.getMessage().getChat().getFirstName() + ". Deleted task:\n " + removedTask);
        sendMessage(buildMessage(chatId, "Задача под номером " + Long.valueOf(messageParts[1]) + " успешно удалена."));
    }

    // /get_tasks_for_today
    private void getTasksForTodayCommandReceived(long chatId, Update update) {
        Map<LocalTime, Task> tasks = taskService.getTasksForToday(chatId);
        if (!tasks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Ваши задачи на ").append(LocalDate.now()).append(": \n").append(separator).append("\n");
            for (Task task : tasks.values()) {
                sb.append("Id: ").append(task.getId()).append("\n")
                        .append(task)
                        .append(separator).append("\n");
            }
            sendMessage(buildMessage(chatId, sb.toString()));
        } else {
            sendMessage(buildMessage(chatId, "Список задач на сегодня пуст"));
        }
        log.info("/get_tasks_for_today command used by " + update.getCallbackQuery().getMessage().getChat().getFirstName());

    }

    // ================================================================================================================
    // Notifications

    @Scheduled(cron = "0 0 6 * * *")
    private void sendAllUsersTasksForToday() {
        Map<Long, Map<LocalTime, Task>> tasksByDay = taskService.getTaskByDayForAll();
        for (Map.Entry<Long, Map<LocalTime, Task>> entry : tasksByDay.entrySet()) {
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

    @Scheduled(cron = "0 * * * * *")
    private void sendAllUsersTaskNotification() {
        Map<Long, Map<LocalTime, Task>> tasksByDay = taskService.getTaskByDayForAll();
        for (Map.Entry<Long, Map<LocalTime, Task>> entry : tasksByDay.entrySet()) {
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
                    if (taskEntry.getKey().minusMinutes(30).getHour() == LocalTime.now().getHour() && taskEntry.getKey().minusMinutes(30).getMinute() == LocalTime.now().getMinute()) {
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
    }

    private void setMorningNotificationCommandReceived(Long chatId, Update update) {
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
        log.info("/set_morning_notification command used by " + update.getCallbackQuery().getMessage().getChat().getFirstName());
    }

    private void setBeforeTaskNotificationCommandReceived(Long chatId, Update update) {
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
        log.info("/set_before_task_notification command used by " + update.getCallbackQuery().getMessage().getChat().getFirstName());
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
            log.error("Error occurred: " + e.getMessage());
        }
    }

    private void menu(String command, Long chatId, Update update) {
        command = command.strip().split("\s", 2)[0];
        switch (command) {
            case "/id" -> idCommandReceived(chatId, update.getMessage().getChat().getId(), update);
            case "/menu", "/start" -> menuCommandReceived(chatId, update);
            case "/create_task" -> createTaskCommandReceived(chatId, update);
            case "/get_tasks" -> getTasksCommandReceived(chatId, update);
            case "/delete_task" -> deleteTaskCommandReceived(chatId, update);
            case "/get_tasks_for_today" -> getTasksForTodayCommandReceived(chatId, update);
            case "/set_morning_notification" -> setMorningNotificationCommandReceived(chatId, update);
            case "/set_before_task_notification" -> setBeforeTaskNotificationCommandReceived(chatId, update);
            default -> buildMessage(chatId, "Command was not recognize");
        }
    }

}
