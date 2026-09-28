package com.lu4ikson.birthday;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
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
    public interface ImportCallback {
        void onImportFinished(List<Person> insertedPeople, int skippedCount);
    }

    public void importPeople(List<Person> newPeople, ImportCallback callback) {
        executorService.execute(() -> {
            List<Person> existing = personDao.getAllPeopleSync();
            List<Person> inserted = new ArrayList<>();
            int skipped = 0;

            for (Person candidate : newPeople) {
                if (isDuplicate(candidate, existing)) {
                    skipped++;
                    continue;
                }
                long id = personDao.insert(candidate);
                candidate.id = (int) id;
                inserted.add(candidate);
                existing.add(candidate); // чтобы не задублировать и внутри самого файла
            }

            if (callback != null) {
                callback.onImportFinished(inserted, skipped);
            }
        });
    }

    private boolean isDuplicate(Person candidate, List<Person> existing) {
        for (Person e : existing) {
            boolean sameName = e.name.trim().equalsIgnoreCase(candidate.name.trim());
            boolean sameDate = e.day == candidate.day && e.month == candidate.month;
            if (sameName && sameDate) {
                return true;
            }
        }
        return false;
    }
}