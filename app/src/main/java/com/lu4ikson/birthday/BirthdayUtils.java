package com.lu4ikson.birthday;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BirthdayUtils {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM");

    // Строит дату день+месяц+год, безопасно обрабатывая 29 февраля на невисокосный год
    public static LocalDate safeDate(int day, int month, int year) {
        try {
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            // 29 февраля на невисокосный год -> считаем как 28 февраля
            if (day == 29 && month == 2) {
                return LocalDate.of(year, 2, 28);
            }
            return null;
        }
    }

    // Возвращает ближайшее вхождение дня рождения (в диапазоне -1..+7 дней от сегодня) или null
    private static UpcomingBirthday resolve(Person person, LocalDate today) {
        LocalDate base = safeDate(person.day, person.month, today.getYear());
        if (base == null) return null;

        LocalDate[] candidates = {base.minusYears(1), base, base.plusYears(1)};

        for (LocalDate candidate : candidates) {
            long diff = ChronoUnit.DAYS.between(today, candidate);
            if (diff >= -1 && diff <= 7) {
                return new UpcomingBirthday(person, (int) diff, candidate);
            }
        }
        return null;
    }

    // Строит и сортирует список для главного экрана
    public static List<UpcomingBirthday> getUpcoming(List<Person> allPeople) {
        LocalDate today = LocalDate.now();
        List<UpcomingBirthday> result = new ArrayList<>();
        for (Person person : allPeople) {
            UpcomingBirthday item = resolve(person, today);
            if (item != null) {
                result.add(item);
            }
        }
        result.sort((a, b) -> Integer.compare(a.dayOffset, b.dayOffset));
        return result;
    }

    // Склонение слова "день"
    private static String daysWord(int n) {
        int n100 = n % 100;
        int n10 = n % 10;
        if (n100 >= 11 && n100 <= 14) return "дней";
        if (n10 == 1) return "день";
        if (n10 >= 2 && n10 <= 4) return "дня";
        return "дней";
    }

    // Склонение слова "год"
    private static String yearsWord(int n) {
        int n100 = n % 100;
        int n10 = n % 10;
        if (n100 >= 11 && n100 <= 14) return "лет";
        if (n10 == 1) return "год";
        if (n10 >= 2 && n10 <= 4) return "года";
        return "лет";
    }


    // Формирует итоговый текст строки списка
    public static String formatText(UpcomingBirthday item) {
        String datePart = item.occurrenceDate.format(DATE_FORMAT);
        String agePart = "";
        if (item.person.year != null) {
            int age = item.occurrenceDate.getYear() - item.person.year;
            agePart = " (" + age + " " + yearsWord(age) + ")";
        }

        if (item.dayOffset == -1) {
            return "Вчера был день рождения " + item.person.name + agePart;
        } else if (item.dayOffset == 0) {
            return "Сегодня день рождения " + item.person.name + agePart;
        } else {
            return "Через " + item.dayOffset + " " + daysWord(item.dayOffset)
                    + " (" + datePart + ") будет день рождения " + item.person.name + agePart;
        }
    }

    //Метод форматирования для полного списка
    public static String formatFullText(Person person) {
        String datePart = String.format("%02d.%02d", person.day, person.month);
        if (person.year != null) {
            return datePart + "." + person.year + " — " + person.name;
        } else {
            return datePart + " — " + person.name;
        }
    }
}