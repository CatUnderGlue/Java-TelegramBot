package ru.catunderglue.telegramspringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.catunderglue.telegramspringbot.model.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findAllByUserId(long userId);
    boolean existsByIdAndUserId(int id, long userId);
}
