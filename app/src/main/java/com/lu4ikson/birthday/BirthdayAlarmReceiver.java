package com.lu4ikson.birthday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class BirthdayAlarmReceiver extends BroadcastReceiver {

    public static final String EXTRA_PERSON_ID = "person_id";
    public static final String EXTRA_PERSON_NAME = "person_name";
    public static final String EXTRA_IS_DAY_BEFORE = "is_day_before";
    public static final String EXTRA_DAY = "day";
    public static final String EXTRA_MONTH = "month";
    public static final String EXTRA_NOTIFY_HOUR = "notify_hour";
    public static final String EXTRA_NOTIFY_MINUTE = "notify_minute";

    @Override
    public void onReceive(Context context, Intent intent) {
        int personId = intent.getIntExtra(EXTRA_PERSON_ID, -1);
        String name = intent.getStringExtra(EXTRA_PERSON_NAME);
        boolean isDayBefore = intent.getBooleanExtra(EXTRA_IS_DAY_BEFORE, false);
        int day = intent.getIntExtra(EXTRA_DAY, -1);
        int month = intent.getIntExtra(EXTRA_MONTH, -1);
        int notifyHour = intent.getIntExtra(EXTRA_NOTIFY_HOUR, 12);
        int notifyMinute = intent.getIntExtra(EXTRA_NOTIFY_MINUTE, 0);

        if (name == null || day == -1 || month == -1) return;

        showNotification(context, personId, name, isDayBefore);

        // план уведомления на след год
        AlarmScheduler.scheduleOne(context, personId, name, day, month, isDayBefore, notifyHour, notifyMinute);
    }

    private void showNotification(Context context, int personId, String name, boolean isDayBefore) {
        String title = isDayBefore ? "Завтра день рождения" : "Сегодня день рождения";
        String text = isDayBefore ? (name + " — завтра") : (name + " — сегодня!");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        int notificationId = personId * 10 + (isDayBefore ? 0 : 1);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            manager.notify(notificationId, builder.build());
        }
    }
}