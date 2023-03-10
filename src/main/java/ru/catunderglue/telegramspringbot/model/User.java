package ru.catunderglue.telegramspringbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "telegram_id")
    private Integer telegramId;
    @Column(name = "name")
    private String name;
}
