package ru.catunderglue.telegramspringbot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.service.FileService;
import ru.catunderglue.telegramspringbot.service.NotificationService;
import ru.catunderglue.telegramspringbot.service.TaskService;
import ru.catunderglue.telegramspringbot.service.ValidationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {
    private Map<Long, Map<Long, Task>> taskMap = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final FileService fileService;
    private final ValidationService validationService;
    private final NotificationService notificationService;

    public TaskServiceImpl(FileService fileService,
                           ValidationService validationService,
                           NotificationService notificationService) {
        this.fileService = fileService;
        this.validationService = validationService;
        this.notificationService = notificationService;
    }

    // ================================================================================================================
    // Tasks
    @Override
    public Long createTask(String title, String description, String date, String time, long userId){
        LocalDate parsedDate = LocalDate.parse(date);
        LocalTime parsedTime = LocalTime.parse(time);
        if (!taskMap.containsKey(userId)){
            taskMap.put(userId, new HashMap<>());
        }
        Map<Long, Task> tasks = taskMap.get(userId);
        long id = 0;
        for (Long aLong : tasks.keySet()) {
            if (aLong > id){
                id = aLong;
            }
        }
        tasks.put(++id, new Task(title, description, parsedDate, parsedTime));
        taskMap.put(userId, tasks);
        saveToFile();
        return id;
    }

    @Override
    public Map<Long, Task> getTasks(long userId){
        if (taskMap.containsKey(userId)){
            return taskMap.get(userId);
        }
        return null;
    }

    @Override
    public Set<Long> getUsersIds(){
        return taskMap.keySet();
    }

    @Override
    public Task getTaskById(Long id, long userId){
        return taskMap.get(userId).get(id);
    }

    @Override
    public Task updateTask(Long id, Task task, long userId){
        if (taskMap.containsKey(userId) && taskMap.get(userId).containsKey(id)){
            Task updatedTask = taskMap.get(userId).put(id, task);
            saveToFile();
            return updatedTask;
        }
        return null;
    }

    @Override
    public Task removeTask(Long id, long userId){
        if (taskMap.containsKey(userId) && taskMap.get(userId).containsKey(id)){
            Task removedTask = taskMap.get(userId).remove(id);
            saveToFile();
            return removedTask;
        }
        return null;
    }

    @Override
    public Map<Long, Map<LocalTime, Task>> getTaskByDayForAll(){
        Map<Long, Map<LocalTime, Task>> map = new HashMap<>();
        for (Long userId : getUsersIds()) {
            if (notificationService.checkMorningNotification(userId)) {
                Map<LocalTime, Task> userTasks = getTasksForToday(userId);
                map.put(userId, userTasks);
            }
        }
        return map;
    }

    @Override
    public Map<LocalTime, Task> getTasksForToday(long userId){
        Map<LocalTime, Task> userTasks = new TreeMap<>();
        if (getTasks(userId) != null) {
            for (Map.Entry<Long, Task> entry : getTasks(userId).entrySet()) {
                entry.getValue().setId(entry.getKey());
                if (validationService.checkDateMatch(userId ,entry.getValue())) {
                    userTasks.put(entry.getValue().getTime(), entry.getValue());
                }
            }
        }
        return userTasks;
    }

    // ================================================================================================================
    // Files
    @PostConstruct
    private void init() {
        readFromFile();
    }

    private void saveToFile() {
        try {
            String json = mapper.writeValueAsString(taskMap);
            fileService.saveTasksToFile(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
        String json = fileService.readTasksFromFile();
        try {
            if (!json.isBlank()) {
                taskMap = mapper.readValue(json, new TypeReference<Map<Long, Map<Long, Task>>>() {
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
