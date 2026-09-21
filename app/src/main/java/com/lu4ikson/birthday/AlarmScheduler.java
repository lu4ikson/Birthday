package com.lu4ikson.birthday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AlarmScheduler {

    //планирует все уведомления для человека
    public static void scheduleAll(Context context, Person person) {
        cancelAll(context, person.id);

        if (person.notifyDayBefore && person.notifyHour != null && person.notifyMinute != null) {
            scheduleOne(context, person.id, person.name, person.day, person.month,
                    true, person.notifyHour, person.notifyMinute);
        }

        scheduleOne(context, person.id, person.name, person.day, person.month,
                false, 12, 0);
    }

    public static void cancelAll(Context context, int personId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        alarmManager.cancel(buildCancelPendingIntent(context, personId, true));
        alarmManager.cancel(buildCancelPendingIntent(context, personId, false));
    }

    public static void scheduleOne(Context context, int personId, String name, int day, int month,
                                   boolean isDayBefore, int hour, int minute) {
        int dayShift = isDayBefore ? -1 : 0;
        LocalDateTime trigger = nextOccurrence(day, month, dayShift, hour, minute);
        setExactAlarm(context, trigger, personId, name, day, month, isDayBefore, hour, minute);
    }


    private static LocalDateTime nextOccurrence(int day, int month, int dayShift, int hour, int minute) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        for (int i = 0; i < 2; i++) {
            LocalDate birthday = BirthdayUtils.safeDate(day, month, currentYear + i);
            if (birthday == null) continue;

            LocalDate targetDate = birthday.plusDays(dayShift);
            LocalDateTime trigger = targetDate.atTime(hour, minute);

            if (trigger.isAfter(LocalDateTime.now())) {
                return trigger;
            }
        }
        return LocalDateTime.now().plusYears(1); // подстраховка
    }

    private static void setExactAlarm(Context context, LocalDateTime trigger, int personId, String name,
                                      int day, int month, boolean isDayBefore, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        long triggerMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        Intent intent = new Intent(context, BirthdayAlarmReceiver.class);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_PERSON_ID, personId);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_PERSON_NAME, name);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_DAY, day);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_MONTH, month);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_IS_DAY_BEFORE, isDayBefore);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_NOTIFY_HOUR, hour);
        intent.putExtra(BirthdayAlarmReceiver.EXTRA_NOTIFY_MINUTE, minute);

        int requestCode = personId * 10 + (isDayBefore ? 0 : 1);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
        }
    }

    private static PendingIntent buildCancelPendingIntent(Context context, int personId, boolean isDayBefore) {
        Intent intent = new Intent(context, BirthdayAlarmReceiver.class);
        int requestCode = personId * 10 + (isDayBefore ? 0 : 1);
        return PendingIntent.getBroadcast(
                context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}