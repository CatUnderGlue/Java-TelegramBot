package ru.catunderglue.telegramspringbot.service.impl;

import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.model.User;
import ru.catunderglue.telegramspringbot.repository.TaskRepository;
import ru.catunderglue.telegramspringbot.repository.UserRepository;
import ru.catunderglue.telegramspringbot.service.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {
    private final ValidationService validationService;
    private final NotificationService notificationService;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(ValidationService validationService,
                           NotificationService notificationService, TaskRepository taskRepository, UserRepository userRepository) {
        this.validationService = validationService;
        this.notificationService = notificationService;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    // ================================================================================================================
    // Tasks
    @Override
    public Task create(String title, String description, String date, String time, long userId) {
        return taskRepository.save(taskVerification(title, description, date, time, userId));
    }

    private Task taskVerification(String title, String description, String date, String time, long userId) throws DateTimeParseException, IllegalArgumentException {
        date = validateDate(date);
        time = validateTime(time);
        LocalDate parsedDate = LocalDate.parse(date);
        LocalTime parsedTime = LocalTime.parse(time);
        return new Task(userId, title, description, parsedDate, parsedTime);
    }

    @Override
    public List<Task> readAll() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> readOne(int id) {
        return taskRepository.findById(id);
    }

    @Override
    public boolean update(Task task, int id, long userId) {
        if (taskRepository.existsByIdAndUserId(id, userId)) {
            task.setId(id);
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(int id, long userId) {
        if (taskRepository.existsByIdAndUserId(id, userId)) {
            taskRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public List<Task> getTasksByUser(int userId) {
        return taskRepository.findAllByUserId(userId);
    }

    @Override
    public Map<Integer, Map<LocalTime, Task>> getTaskByDayForAll() {
        Map<Integer, Map<LocalTime, Task>> map = new HashMap<>();
        for (int userId : userRepository.findAll().stream().map(User::getTelegramId).toList()) {
            if (notificationService.checkMorningNotification(userId)) {
                Map<LocalTime, Task> userTasks = getTasksForToday(userId);
                map.put(userId, userTasks);
            }
        }
        return map;
    }

    @Override
    public Map<LocalTime, Task> getTasksForToday(int userId) {
        Map<LocalTime, Task> userTasks = new TreeMap<>();
        if (getTasksByUser(userId) != null || !getTasksByUser(userId).isEmpty()) {
            for (Task task : getTasksByUser(userId)) {
                if (validationService.checkDateMatch(userId, task)) {
                    userTasks.put(task.getTime(), task);
                }
            }

        }
        return userTasks;
    }

    @Override
    public void clearTasks(CheckTask filter) {
        for (int userId : userRepository.findAll().stream().map(User::getTelegramId).toList()) {
            for (Task task : getTasksByUser(userId)) {
                if (filter.checkTask(task)) {
                    taskRepository.delete(task);
                }
            }
        }
    }

    private String validateDate(String date) {
        if (date.isBlank() || !date.matches("\\d{4}(.|-)\\d{2}(.|-)\\d{2}")) {
            throw new IllegalArgumentException("Неверный формат даты.");
        }
        date = date.replaceAll("\\.", "-");
        return date;
    }

    private String validateTime(String time) {
        if (time.isBlank() || !time.matches("\\d{2}(.|:)\\d{2}")) {
            throw new IllegalArgumentException("Неверный формат времени.");
        }
        time = time.replaceAll("\\.", ":");
        return time;
    }
}
