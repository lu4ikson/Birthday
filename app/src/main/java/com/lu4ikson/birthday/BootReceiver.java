package com.lu4ikson.birthday;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.concurrent.Executors;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        PendingResult pendingResult = goAsync();
        Context appContext = context.getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(appContext);
            List<Person> people = db.personDao().getAllPeopleSync();
            for (Person person : people) {
                AlarmScheduler.scheduleAll(appContext, person);
            }
            pendingResult.finish();
        });
    }
}