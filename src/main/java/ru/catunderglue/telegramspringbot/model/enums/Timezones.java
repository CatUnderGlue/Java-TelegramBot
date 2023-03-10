package ru.catunderglue.telegramspringbot.model.enums;

public enum Timezones {
    KALININGRAD("Europe/Kaliningrad"),
    MOSCOW("Europe/Moscow"),
    SAMARA("Europe/Samara"),
    YEKATERINBURG("Asia/Yekaterinburg"),
    OMSK("Asia/Tomsk"),
    KRASNOYARSK("Asia/Krasnoyarsk"),
    IRKUTSK("Asia/Irkutsk"),
    YAKUTSK("Asia/Yakutsk"),
    VLADIVOSTOK("Asia/Vladivostok"),
    SREDNEKOLYMSK("Asia/Srednekolymsk"),
    KAMCHATSK("Asia/Kamchatka");

    final String timezone;

    Timezones(String timezone) {
        this.timezone = timezone;
    }

    public String getTimezone() {
        return timezone;
    }
}
