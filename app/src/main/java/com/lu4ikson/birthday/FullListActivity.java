package com.lu4ikson.birthday;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FullListActivity extends AppCompatActivity {

    private PersonFullAdapter adapter;
    private List<Person> allPeopleCache = new ArrayList<>();
    private String currentQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFullList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PersonViewModel viewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        adapter = new PersonFullAdapter(person -> {
            viewModel.delete(person);
            AlarmScheduler.cancelAll(this, person.id);
        });
        recyclerView.setAdapter(adapter);

        viewModel.getAllPeople().observe(this, people -> {
            allPeopleCache = people;
            applyFilter();
        });

        SearchView searchView = findViewById(R.id.searchViewPeople);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText;
                applyFilter();
                return true;
            }
        });
    }

    private void applyFilter() {
        if (currentQuery.isEmpty()) {
            adapter.setItems(allPeopleCache);
            return;
        }

        String queryLower = currentQuery.toLowerCase(Locale.getDefault());
        List<Person> filtered = new ArrayList<>();
        for (Person person : allPeopleCache) {
            if (person.name.toLowerCase(Locale.getDefault()).contains(queryLower)) {
                filtered.add(person);
            }
        }
        adapter.setItems(filtered);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}