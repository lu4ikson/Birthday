package com.lu4ikson.birthday;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class PersonViewModel extends AndroidViewModel {

    private final PersonRepository repository;
    private final LiveData<List<Person>> allPeople;

    public PersonViewModel(@NonNull Application application) {
        super(application);
        repository = new PersonRepository(application);
        allPeople = repository.getAllPeople();
    }

    public LiveData<List<Person>> getAllPeople() {
        return allPeople;
    }

    public void insert(Person person, PersonRepository.InsertCallback callback) {
        repository.insert(person, callback);
    }

    public void update(Person person) {
        repository.update(person);
    }

    public void delete(Person person) {
        repository.delete(person);
    }
    //метод-обертка
    public void importPeople(List<Person> newPeople, PersonRepository.ImportCallback callback) {
        repository.importPeople(newPeople, callback);
    }
}