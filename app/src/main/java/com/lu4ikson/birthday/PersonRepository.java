package com.lu4ikson.birthday;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PersonRepository {

    private final PersonDao personDao;
    private final LiveData<List<Person>> allPeople;
    private final ExecutorService executorService;

    public PersonRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        personDao = db.personDao();
        allPeople = personDao.getAllPeople();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Person>> getAllPeople() {
        return allPeople;
    }

    public void insert(Person person, InsertCallback callback) {
        executorService.execute(() -> {
            long id = personDao.insert(person);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void update(Person person) {
        executorService.execute(() -> personDao.update(person));
    }

    public void delete(Person person) {
        executorService.execute(() -> personDao.delete(person));
    }

    public interface InsertCallback {
        void onInserted(long id);
    }
}