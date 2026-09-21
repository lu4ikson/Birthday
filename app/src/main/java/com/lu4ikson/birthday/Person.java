package com.lu4ikson.birthday;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "people")
public class Person {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;
    public int day;    // 1-31
    public int month;  // 1-12
    public Integer year;

    public boolean notifyDayBefore;   // уведомить за день до
    public Integer notifyHour;        // час пуша накануне
    public Integer notifyMinute;      // минута пуша накануне

    public Person(@NonNull String name, int day, int month, Integer year) {
        this.name = name;
        this.day = day;
        this.month = month;
        this.year = year;
        this.notifyDayBefore = false;
        this.notifyHour = null;
        this.notifyMinute = null;
    }
}