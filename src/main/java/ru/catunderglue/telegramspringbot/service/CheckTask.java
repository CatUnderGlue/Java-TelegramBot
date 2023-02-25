package ru.catunderglue.telegramspringbot.service;

import ru.catunderglue.telegramspringbot.model.Task;

public interface CheckTask {
    boolean checkTask(Task task);
}
