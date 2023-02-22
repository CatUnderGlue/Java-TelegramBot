package ru.catunderglue.telegramspringbot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.service.FileService;
import ru.catunderglue.telegramspringbot.service.NotificationService;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationServiceImpl implements NotificationService {
    private enum Notification {
        MORNING, BEFORE_TASK
    }

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    HashMap<Long, Map<Notification, Boolean>> usersNotifications = new HashMap<>();

    private final FileService fileService;

    public NotificationServiceImpl(FileService fileService) {
        this.fileService = fileService;
    }

    @Override
    public void setMorningNotification(Long userId, Boolean toggle) {
        setNotification(userId, Notification.MORNING, toggle);
    }

    @Override
    public void setBeforeTaskNotification(Long userId, Boolean toggle) {
        setNotification(userId, Notification.BEFORE_TASK, toggle);
    }

    @Override
    public void setAllNotification(Long userId, Boolean toggle) {
        setMorningNotification(userId, toggle);
        setBeforeTaskNotification(userId, toggle);
    }

    @Override
    public boolean checkMorningNotification(Long userId) {
        if (usersNotifications.containsKey(userId)) {
            return usersNotifications.get(userId).get(Notification.MORNING);
        }
        return false;
    }

    @Override
    public boolean checkBeforeTaskNotification(Long userId) {
        return usersNotifications.get(userId).get(Notification.BEFORE_TASK);
    }

    @Override
    public boolean isContains(Long userId) {
        return usersNotifications.containsKey(userId);
    }

    private void setNotification(Long userId, Notification notification, Boolean toggle) {
        if (!usersNotifications.containsKey(userId)) {
            usersNotifications.put(userId, new HashMap<>());
        }
        usersNotifications.get(userId).put(notification, toggle);
        saveToFile();
    }

    // ================================================================================================================
    // Files
    @PostConstruct
    private void init() {
        readFromFile();
    }

    private void saveToFile() {
        try {
            String json = mapper.writeValueAsString(usersNotifications);
            fileService.saveNotificationsToFile(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
        String json = fileService.readNotificationsFromFile();
        try {
            if (!json.isBlank()) {
                usersNotifications = mapper.readValue(json, new TypeReference<HashMap<Long, Map<Notification, Boolean>>>() {
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
