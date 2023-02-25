package ru.catunderglue.telegramspringbot.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
public class Task implements Comparable<Task>{
    @NotBlank
    private String title;
    @NotBlank
    private String description;
    private long id;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}")
    private LocalDate date;
    @Pattern(regexp = "\\d{2}:\\d{2}")
    private LocalTime time;

    public Task(String title, String description, LocalDate date, LocalTime time) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
    }

    @Override
    public String toString() {
        return title + "\n" +
                description + "\n" +
                "Дата выполнения: " + date + " Время: " + time + "\n";
    }

    @Override
    public int compareTo(Task task) {
        int res;
        if (this.date.isBefore(task.getDate())){
            res = -1;
        } else if (this.date.isAfter(task.getDate())) {
            res = 1;
        } else {
            if (this.time.isBefore(task.getTime())){
                res = -1;
            } else if (this.time.isAfter(task.getTime())) {
                res = 1;
            } else {
                res = 0;
            }
        }
        return res;
    }
}
