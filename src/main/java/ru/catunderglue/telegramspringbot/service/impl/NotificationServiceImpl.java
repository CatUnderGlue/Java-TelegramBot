package ru.catunderglue.telegramspringbot.service.impl;


import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.Timezone;
import ru.catunderglue.telegramspringbot.model.enums.Timezones;
import ru.catunderglue.telegramspringbot.repository.NotificationRepository;
import ru.catunderglue.telegramspringbot.repository.TimezoneRepository;
import ru.catunderglue.telegramspringbot.service.NotificationService;

import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationService {
    private enum Notification {
        MORNING, BEFORE_TASK
    }

    private final TimezoneRepository usersTimezones;
    private final NotificationRepository usersNotifications;

    public NotificationServiceImpl(TimezoneRepository usersTimezones, NotificationRepository usersNotifications) {
        this.usersTimezones = usersTimezones;
        this.usersNotifications = usersNotifications;
    }

    @Override
    public boolean setMorningNotification(Integer userId, Boolean toggle) {
        if (usersNotifications.existsById(userId)){
            Optional<ru.catunderglue.telegramspringbot.model.Notification> notification = usersNotifications.findById(userId);
            notification.ifPresent(value -> value.setMorning(toggle));
            usersNotifications.save(notification.get());
            return true;
        }
        return false;
    }

    @Override
    public boolean setBeforeTaskNotification(Integer userId, Boolean toggle) {
        if (usersNotifications.existsById(userId)){
            Optional<ru.catunderglue.telegramspringbot.model.Notification> notification = usersNotifications.findById(userId);
            notification.ifPresent(value -> value.setBeforeTask(toggle));
            usersNotifications.save(notification.get());
            return true;
        }
        return false;
    }

    @Override
    public void setAllNotification(Integer userId, Boolean toggle) {
        if (!setMorningNotification(userId, toggle) && !setBeforeTaskNotification(userId, toggle)){
            usersNotifications.save(new ru.catunderglue.telegramspringbot.model.Notification(userId, toggle, toggle));
        }
    }

    @Override
    public void setUserTimezone(Integer userId, Timezones timezones){
        usersTimezones.save(new Timezone(userId, timezones.getTimezone()));
    }

    @Override
    public String getUserTimezone(Integer userId){
        if (usersTimezones.existsById(userId)){
            return usersTimezones.findById(userId).get().getName();
        }
        return null;
    }

    @Override
    public boolean checkMorningNotification(Integer userId) {
        Optional<ru.catunderglue.telegramspringbot.model.Notification> notification = usersNotifications.findById(userId);
        if (usersNotifications.existsById(userId) && notification.isPresent()) {
            return notification.get().isMorning();
        }
        return false;
    }

    @Override
    public boolean checkBeforeTaskNotification(Integer userId) {
        Optional<ru.catunderglue.telegramspringbot.model.Notification> notification = usersNotifications.findById(userId);
        if (usersNotifications.existsById(userId) && notification.isPresent()) {
            return notification.get().isBeforeTask();
        }
        return false;
    }

    @Override
    public boolean isContains(Integer userId) {
        return usersNotifications.existsById(userId);
    }

    @Override
    public boolean isTimezoneContains(Integer userId) {
        return usersTimezones.existsById(userId);
    }
}
