package com.lu4ikson.birthday;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FullListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_list);

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFullList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PersonViewModel viewModel = new ViewModelProvider(this).get(PersonViewModel.class);

        PersonFullAdapter adapter = new PersonFullAdapter(person -> {
            viewModel.delete(person);
            AlarmScheduler.cancelAll(this, person.id);
        });
        recyclerView.setAdapter(adapter);
        viewModel.getAllPeople().observe(this, adapter::setItems);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}