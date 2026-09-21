package com.lu4ikson.birthday;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PersonDao {

    @Insert
    long insert(Person person);

    @Update
    void update(Person person);

    @Delete
    void delete(Person person);

    @Query("SELECT * FROM people ORDER BY month, day")
    LiveData<List<Person>> getAllPeople();

    @Query("SELECT * FROM people")
    List<Person> getAllPeopleSync();
}