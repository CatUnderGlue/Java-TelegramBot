package ru.catunderglue.telegramspringbot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @Column(name = "user_id")
    private int id;

    @Column(name = "morning")
    private boolean morning;

    @Column(name = "before_task")
    private boolean beforeTask;
}
