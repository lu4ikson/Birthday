package com.lu4ikson.birthday;

import java.time.LocalDate;

public class UpcomingBirthday {
    public final Person person;
    public final int dayOffset;   // -1 = вчера, 0 = сегодня, 1..7 = через сколько дней
    public final LocalDate occurrenceDate; // конкретная дата этого дня рождения (с правильным годом)

    public UpcomingBirthday(Person person, int dayOffset, LocalDate occurrenceDate) {
        this.person = person;
        this.dayOffset = dayOffset;
        this.occurrenceDate = occurrenceDate;
    }
}