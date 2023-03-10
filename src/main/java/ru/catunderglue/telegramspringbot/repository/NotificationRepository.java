package ru.catunderglue.telegramspringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.catunderglue.telegramspringbot.model.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
}
