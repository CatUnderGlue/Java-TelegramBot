package ru.catunderglue.telegramspringbot.service.impl;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.catunderglue.telegramspringbot.service.FileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileServiceImpl implements FileService {

    @Value(value = "${data.files.path}")
    private String dataFilesPath;

    @Value(value = "${tasks.data.file.name}")
    private String tasksDataFileName;

    @Value(value = "${notifications.data.file.name}")
    private String notificationsDataFileName;

    @Override
    public boolean saveTasksToFile(String json) {
        return saveToFile(json, tasksDataFileName);
    }

    @Override
    public String readTasksFromFile() {
        return readFromFile(tasksDataFileName);
    }

    @Override
    public boolean saveNotificationsToFile(String json){
        return saveToFile(json, notificationsDataFileName);
    }

    @Override
    public String readNotificationsFromFile() {
        return readFromFile(notificationsDataFileName);
    }

    public void cleanDataFile(String dataFileName) {
        Path path = Path.of(dataFilesPath, dataFileName);
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readFromFile(String dataFileName) {
        try {
            return Files.readString(Path.of(dataFilesPath, dataFileName));
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean saveToFile(String json, String dataFileName) {
        try {
            cleanDataFile(dataFileName);
            Files.writeString(Path.of(dataFilesPath, dataFileName), json);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @PostConstruct
    private void init() {
        try {
            if (Files.notExists(Path.of(dataFilesPath, tasksDataFileName))) {
                Files.createFile(Path.of(dataFilesPath, tasksDataFileName));
            }
            if (Files.notExists(Path.of(dataFilesPath, notificationsDataFileName))) {
                Files.createFile(Path.of(dataFilesPath, notificationsDataFileName));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
