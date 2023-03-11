package ru.catunderglue.telegramspringbot;

import ru.catunderglue.telegramspringbot.model.Task;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ContantsForTaskServiceTest {
    public static final String BLANK = " ";
    public static final String TITLE = "Title";
    public static final String DESCRIPTION = "Description";
    public static final String VALID_STRING_DATE_WITH_HYPHEN = "2023-03-11";
    public static final String VALID_STRING_DATE_WITH_DOT = "2023.03.11";
    public static final String INVALID_STRING_DATE = "2.2.2";
    public static final LocalDate VALID_DATE = LocalDate.parse(VALID_STRING_DATE_WITH_HYPHEN);
    public static final String VALID_STRING_TIME_WITH_DOT = "10.00";
    public static final String VALID_STRING_TIME_WITH_COLON = "10:00";
    public static final String INVALID_STRING_TIME = "10";
    public static final LocalTime VALID_TIME = LocalTime.parse(VALID_STRING_TIME_WITH_COLON);
    public static final Long VALID_USER_ID = 469391449L;
    public static final Task VALID_TASK = new Task(VALID_USER_ID, TITLE, DESCRIPTION, VALID_DATE, VALID_TIME);
    public static final List<Task> TASKS_LIST = List.of(VALID_TASK, VALID_TASK, VALID_TASK);
    public static final Integer TASK_ID = 0;
}
