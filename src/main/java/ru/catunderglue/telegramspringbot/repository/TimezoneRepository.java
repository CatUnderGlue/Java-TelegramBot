package ru.catunderglue.telegramspringbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.catunderglue.telegramspringbot.model.Timezone;

public interface TimezoneRepository extends JpaRepository<Timezone, Integer> {
}
