package ru.catunderglue.telegramspringbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task implements Comparable<Task>{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "time")
    private LocalTime time;

    public Task(long userId, String title, String description, LocalDate date, LocalTime time) {
        this.userId = userId;
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
