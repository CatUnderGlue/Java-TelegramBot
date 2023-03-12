package ru.catunderglue.telegramspringbot.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.catunderglue.telegramspringbot.model.Task;
import ru.catunderglue.telegramspringbot.repository.NotificationRepository;
import ru.catunderglue.telegramspringbot.repository.TaskRepository;
import ru.catunderglue.telegramspringbot.repository.TimezoneRepository;
import ru.catunderglue.telegramspringbot.repository.UserRepository;
import ru.catunderglue.telegramspringbot.service.TaskService;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static ru.catunderglue.telegramspringbot.ContantsForTaskServiceTest.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {
    static TaskRepository taskRepositoryMock = Mockito.mock(TaskRepository.class);
    static UserRepository userRepositoryMock = Mockito.mock(UserRepository.class);
    static TimezoneRepository timezoneRepositoryMock = Mockito.mock(TimezoneRepository.class);
    static NotificationRepository notificationRepositoryMock = Mockito.mock(NotificationRepository.class);
    static NotificationServiceImpl notificationService = new NotificationServiceImpl(timezoneRepositoryMock, notificationRepositoryMock);
    static ValidationServiceImpl validationService = new ValidationServiceImpl(notificationService);
    static TaskService taskService = new TaskServiceImpl(validationService, notificationService, taskRepositoryMock, userRepositoryMock);

    @BeforeEach
    void reset() {
        Mockito.reset(taskRepositoryMock);
    }

    @Test
    void shouldCreateTaskWithAllArgs() {
        when(taskRepositoryMock.save(VALID_TASK)).thenReturn(VALID_TASK);
        Task actual = taskService.create(TITLE, DESCRIPTION, VALID_STRING_DATE_WITH_DOT, VALID_STRING_TIME_WITH_DOT, VALID_USER_ID);
        assertEquals(VALID_TASK, actual);
        verify(taskRepositoryMock, times(1)).save(VALID_TASK);
    }

    @Test
    void shouldThrowIllegalArgExcWhenCreateTaskWithIllegalTitleDescDateOrTime() {
        when(taskRepositoryMock.save(VALID_TASK)).thenReturn(VALID_TASK);
        assertThrows(IllegalArgumentException.class, () -> taskService
                .create(BLANK, DESCRIPTION, VALID_STRING_DATE_WITH_HYPHEN, VALID_STRING_TIME_WITH_COLON, VALID_USER_ID));
        assertThrows(IllegalArgumentException.class, () -> taskService
                .create(TITLE, BLANK, VALID_STRING_DATE_WITH_HYPHEN, VALID_STRING_TIME_WITH_COLON, VALID_USER_ID));
        assertThrows(IllegalArgumentException.class, () -> taskService
                .create(TITLE, DESCRIPTION, INVALID_STRING_DATE, VALID_STRING_TIME_WITH_COLON, VALID_USER_ID));
        assertThrows(IllegalArgumentException.class, () -> taskService
                .create(TITLE, DESCRIPTION, VALID_STRING_DATE_WITH_HYPHEN, INVALID_STRING_TIME, VALID_USER_ID));
    }

    @Test
    void shouldReturnListOfTasks() {
        when(taskRepositoryMock.findAll()).thenReturn(TASKS_LIST);
        assertEquals(TASKS_LIST, taskService.readAll());
        verify(taskRepositoryMock, times(1)).findAll();
    }

    @Test
    void shouldReturnTaskFoundById() {
        Optional<Task> optionalTask = Optional.of(VALID_TASK);
        when(taskRepositoryMock.findById(TASK_ID)).thenReturn(optionalTask);
        assertEquals(optionalTask, taskService.readOne(TASK_ID));
        verify(taskRepositoryMock, times(1)).findById(TASK_ID);
    }

    @Test
    void shouldReturnEmptyTaskFoundById() {
        Optional<Task> optionalTask = Optional.empty();
        assertEquals(optionalTask, taskService.readOne(TASK_ID));
        verify(taskRepositoryMock, times(1)).findById(TASK_ID);
    }

    @Test
    void shouldReturnTrueAfterUpdateTask() {
        when(taskRepositoryMock.existsByIdAndUserId(TASK_ID, VALID_USER_ID)).thenReturn(true);
        when(taskRepositoryMock.save(VALID_TASK)).thenReturn(VALID_TASK);
        assertTrue(taskService.update(VALID_TASK, TASK_ID, VALID_USER_ID));
        verify(taskRepositoryMock).existsByIdAndUserId(TASK_ID, VALID_USER_ID);
        verify(taskRepositoryMock).save(VALID_TASK);
    }

    @Test
    void shouldReturnFalseAfterUpdateTask() {
        when(taskRepositoryMock.existsByIdAndUserId(TASK_ID, VALID_USER_ID)).thenReturn(false);
        assertFalse(taskService.update(VALID_TASK, TASK_ID, VALID_USER_ID));
        verify(taskRepositoryMock).existsByIdAndUserId(TASK_ID, VALID_USER_ID);
    }

    @Test
    void shouldReturnTrueAfterDeleteTask() {
        when(taskRepositoryMock.existsByIdAndUserId(TASK_ID, VALID_USER_ID)).thenReturn(true);
        assertTrue(taskService.delete(TASK_ID, VALID_USER_ID));
        verify(taskRepositoryMock).existsByIdAndUserId(TASK_ID, VALID_USER_ID);
        verify(taskRepositoryMock).deleteById(TASK_ID);
    }

    @Test
    void shouldReturnFalseAfterDeleteTask() {
        when(taskRepositoryMock.existsByIdAndUserId(TASK_ID, VALID_USER_ID)).thenReturn(false);
        assertFalse(taskService.delete(TASK_ID, VALID_USER_ID));
        verify(taskRepositoryMock).existsByIdAndUserId(TASK_ID, VALID_USER_ID);
    }

    @Test
    void shouldReturnListOfTasksFoundByUserId() {
        when(taskRepositoryMock.findAllByUserId(VALID_USER_ID)).thenReturn(TASKS_LIST);
        assertEquals(TASKS_LIST, taskService.getTasksByUser(Math.toIntExact(VALID_USER_ID)));
        verify(taskRepositoryMock, times(1)).findAllByUserId(VALID_USER_ID);
    }
}