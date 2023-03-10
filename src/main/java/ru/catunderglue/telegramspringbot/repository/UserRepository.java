package ru.catunderglue.telegramspringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.catunderglue.telegramspringbot.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}
