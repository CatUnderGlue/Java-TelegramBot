package ru.catunderglue.telegramspringbot.service.impl;


import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.service.NotificationService;
import ru.catunderglue.telegramspringbot.service.ValidationService;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class ValidationServiceImpl implements ValidationService {

    private final NotificationService notificationService;

    public ValidationServiceImpl(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public boolean checkDateMatch(Long userId ,Task task) {
        String timezone = notificationService.getUserTimezone(userId).getTimezone();
        ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of(timezone));
        return task.getDate().equals(dateTime.toLocalDate());
    }
}
