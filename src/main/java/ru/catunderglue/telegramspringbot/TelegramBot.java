package ru.catunderglue.telegramspringbot;

import static ru.catunderglue.telegramspringbot.CommandsForMenu.*;

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
        // ???????????????????????????? ???????? ????????????, ???????? ???????????????????????? ???????????? ?????? ???????????????????? ????????
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
        if (update.getMessage() == null || update.getMessage().getText().equals(CREATE_TASK)) {
            sendMessage(buildMessage(chatId, "?????????? ?????????????? ????????????, ?????????????? ?????? ?????????????? ?? ?????????? ??????????????:\n?????????????? \"|\" ??????????????????????."));
            sendMessage(buildMessage(chatId, String.format("```\n%s ????????????????|????????????????|%s|00:00\n```", CREATE_TASK ,now)));
        } else {
            try {
                String[] messageParts = update.getMessage().getText().split("\s", 2);
                String[] taskParts = messageParts[1].split("\\|");
                taskService.create(taskParts[0], taskParts[1], taskParts[2], taskParts[3], chatId);
                sendMessage(buildMessage(chatId, "???????????? ?????????????? ??????????????!"));
            } catch (IndexOutOfBoundsException e) {
                sendMessage(buildMessage(chatId, "???????????????? ???????????? ??????????????."));
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
            sendMessage(buildMessage(chatId, "?? ?????? ???????? ?????? ?????? ?????????? :c"));
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
        if (update.getMessage() == null || update.getMessage().getText().equals(UPDATE_TASK)) {
            sendMessage(buildMessage(chatId, "?????????? ???????????????? ????????????, ?????????????? ?????? ?????????????? ?? ?????????? ??????????????:\n?????????????? \"|\" ??????????????????????."));
            sendMessage(buildMessage(chatId, String.format("```\n%s id ????????????????|????????????????|%s|00:00\n```", UPDATE_TASK, now)));
        } else {
            try {
                String[] messageParts = update.getMessage().getText().split("\s", 3);
                int id = Integer.parseInt(messageParts[1]);
                String[] taskParts = messageParts[2].split("\\|");
                LocalDate parsedDate = LocalDate.parse(taskParts[2]);
                LocalTime parsedTime = LocalTime.parse(taskParts[3]);
                Task updateTask = new Task(chatId, taskParts[0], taskParts[1], parsedDate, parsedTime);
                taskService.update(updateTask, id, chatId);
                sendMessage(buildMessage(chatId, String.format("???????????? ?????? ?????????????? %d ?????????????? ??????????????????!", id)));
            } catch (Exception e) {
                sendMessage(buildMessage(chatId, "???????????? ?? ???????????????????? ????????????, ?????????????????????????????? ???????????????????? ??????????????."));
            }
        }
    }

    // /delete_task
    private void deleteTaskCommandReceived(long chatId, Update update) {
        if (update.hasCallbackQuery() && update.getCallbackQuery().getData().strip().equals(DELETE_TASK)) {
            sendMessage(buildMessage(chatId, "?????????? ?????????????? ???????????? ?????? ????????????, ?????????????? ?????? ?????????????? ?? ?????????? ??????????????:"));
            sendMessage(buildMessage(chatId,
                    """
                            ```
                            /delete_task id
                            ```
                            """));
            return;
        }
        String[] messageParts = update.getMessage().getText().split("\s", 2);
        if (taskService.delete(Integer.parseInt(messageParts[1]), chatId)) {
            sendMessage(buildMessage(chatId, "???????????? ?????? ?????????????? " + Long.valueOf(messageParts[1]) + " ?????????????? ??????????????."));
            return;
        }
        sendMessage(buildMessage(chatId, "???????????? ?????? ???????????? id ???? ??????????????."));

    }

    // /get_tasks_for_today
    private void getTasksForTodayCommandReceived(int chatId) {
        Map<LocalTime, Task> tasks = taskService.getTasksForToday(chatId);
        String timezone = notificationService.getUserTimezone(chatId);
        if (!tasks.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("???????? ???????????? ???? ").append(ZonedDateTime.now(ZoneId.of(timezone)).toLocalDate()).append(": \n").append(separator).append("\n");
            for (Task task : tasks.values()) {
                sb.append("Id: ").append(task.getId()).append("\n")
                        .append(task)
                        .append(separator).append("\n");
            }
            sendMessage(buildMessage(chatId, sb.toString()));
        } else {
            sendMessage(buildMessage(chatId, "???????????? ?????????? ???? ?????????????? ????????"));
        }
    }

    @Scheduled(cron = "@hourly")
    private void clearOldTask() {
        taskService.clearTasks((Task task) -> task.getDate().plusDays(1).isBefore(LocalDate.now()));
    }

    // ================================================================================================================
    // Notifications

    @Scheduled(cron = "@hourly")
    private void sendAllUsersTasksForToday() {
        try {
            Map<Integer, Map<LocalTime, Task>> tasksByDay = taskService.getTaskByDayForAll();
            for (Map.Entry<Integer, Map<LocalTime, Task>> entry : tasksByDay.entrySet()) {
                String timezone = notificationService.getUserTimezone(entry.getKey());
                if (ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime().getHour() == 6) {
                    if (entry.getValue().size() != 0) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("???????? ???????????? ???? ")
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
                    sb.append("??????????????????, ?????? ?????????? 30 ?????????? ?? ?????? ???????? ?????????????????????????????? ????????????: \n")
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
        if (update.getCallbackQuery().getData().strip().equals(SET_MORNING_NOTIFICATIONS)) {
            SendMessage message = buildMessage(chatId, "?????????????????? ?????????????????? ??????????????????????");
            message.setReplyMarkup(keyboardService.getMorningNotificationKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        boolean flag = switch (messageParts[1]) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("0 - ????????, 1 - ??????");
        };

        try {
            notificationService.setMorningNotification(chatId, flag);
        } catch (IllegalArgumentException e) {
            sendMessage(buildMessage(chatId, e.getMessage()));
            return;
        }

        sendMessage(buildMessage(chatId, "???????????????? ?????????????????????? " + (flag ? "????????????????." : "??????????????????.")));
    }

    private void setBeforeTaskNotificationCommandReceived(int chatId, Update update) {
        if (update.getCallbackQuery().getData().strip().equals(SET_BEFORE_TASK_NOTIFICATION)) {
            SendMessage message = buildMessage(chatId, "?????????????????? ??????????????????????: ???? 30 ?????????? ???? ????????????");
            message.setReplyMarkup(keyboardService.getBeforeTaskNotificationKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        boolean flag = switch (messageParts[1]) {
            case "0" -> false;
            case "1" -> true;
            default -> throw new IllegalArgumentException("0 - ????????, 1 - ??????");
        };

        try {
            notificationService.setBeforeTaskNotification(chatId, flag);
        } catch (IllegalArgumentException e) {
            sendMessage(buildMessage(chatId, e.getMessage()));
            return;
        }

        sendMessage(buildMessage(chatId, "??????????????????????: ???? 30 ?????????? ???? ???????????? " + (flag ? "????????????????." : "??????????????????.")));
    }

    private void setUserTimezoneCommandReceived(int chatId, Update update) {
        if (update.getCallbackQuery().getData().strip().equals(SET_USER_TIMEZONE)) {
            SendMessage message = buildMessage(chatId, "?????????????????? ???????????? ???????????????? ??????????.");
            message.setReplyMarkup(keyboardService.getTimezonesKeyboard());
            sendMessage(message);
            return;
        }
        String[] messageParts = update.getCallbackQuery().getData().split("\s", 2);
        if (messageParts[1].equals("check")) {
            String timezone = notificationService.getUserTimezone(chatId);
            LocalTime now = ZonedDateTime.now(ZoneId.of(timezone)).toLocalTime();
            sendMessage(buildMessage(chatId, "???????? ??????????: " + now.format(DateTimeFormatter.ofPattern("HH:mm")) + "\n?????? ?????????????? ????????: " + timezone));
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

        sendMessage(buildMessage(chatId, "?????? ?????????????? ????????: " + timezones));
    }

    private void notificationsCommandReceived(int chatId) {
        SendMessage message = buildMessage(chatId, "?????????????????? ??????????????????????");
        message.setReplyMarkup(keyboardService.getNotificationsKeyboard());
        sendMessage(message);

    }

    // ================================================================================================================
    // 
    private String userInfo(Update update) {
        return String.format("First name: %s Id: %d About: %s BIO: %s",
                update.getMessage().getChat().getFirstName(), // ??????
                update.getMessage().getChat().getId(), // Id
                update.getMessage().getChat().getDescription() == null ? "-" : update.getMessage().getChat().getDescription(), // ????????????????
                update.getMessage().getChat().getBio() == null ? "-" : update.getMessage().getChat().getBio()); // ??????
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
            case "/id" -> idCommandReceived(id, update.getMessage().getChat().getId()); // temporarily disabled
            case MENU, START -> menuCommandReceived(id, update);
            case CREATE_TASK -> createTaskCommandReceived(id, update);
            case GET_TASKS -> getTasksCommandReceived(id);
            case DELETE_TASK -> deleteTaskCommandReceived(id, update);
            case UPDATE_TASK -> updateTaskCommandReceived(id, update);
            case GET_TASKS_FOR_TODAY -> getTasksForTodayCommandReceived(id);
            case SET_MORNING_NOTIFICATIONS -> setMorningNotificationCommandReceived(id, update);
            case SET_BEFORE_TASK_NOTIFICATION -> setBeforeTaskNotificationCommandReceived(id, update);
            case SET_USER_TIMEZONE -> setUserTimezoneCommandReceived(id, update);
            case NOTIFICATIONS -> notificationsCommandReceived(id);
            default -> buildMessage(chatId, "Command was not recognize");
        }
    }

}
