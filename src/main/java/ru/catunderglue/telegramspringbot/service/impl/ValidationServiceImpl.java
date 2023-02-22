package ru.catunderglue.telegramspringbot.service.impl;

import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.service.ValidationService;

import java.time.LocalDate;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public boolean checkDateMatch(Task task) {
        return task.getDate().equals(LocalDate.now());
    }
}
