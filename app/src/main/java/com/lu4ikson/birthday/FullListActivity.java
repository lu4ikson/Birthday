package com.lu4ikson.birthday;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FullListActivity extends AppCompatActivity {

    private PersonFullAdapter adapter;
    private PersonViewModel viewModel;
    private List<Person> allPeopleCache = new ArrayList<>();
    private String currentQuery = "";
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("text/csv"),
            uri -> {
                if (uri != null) {
                    doExport(uri);
                }
            });

    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) {
                    doImport(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_list);
        //фикс обработки отступов
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fullListRoot), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerViewFullList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        viewModel = new ViewModelProvider(this).get(PersonViewModel.class);

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

        findViewById(R.id.buttonExport).setOnClickListener(v ->
                exportLauncher.launch("birthday_export.csv"));

        findViewById(R.id.buttonImport).setOnClickListener(v ->
                importLauncher.launch(new String[]{"text/csv", "text/comma-separated-values", "*/*"}));
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

    private void doExport(Uri uri) {
        List<Person> snapshot = allPeopleCache;
        ioExecutor.execute(() -> {
            try (OutputStream out = getContentResolver().openOutputStream(uri)) {
                CsvHelper.exportToStream(out, snapshot);
                runOnUiThread(() -> Toast.makeText(this,
                        "Экспортировано: " + snapshot.size(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Ошибка экспорта: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void doImport(Uri uri) {
        ioExecutor.execute(() -> {
            try (InputStream in = getContentResolver().openInputStream(uri)) {
                List<Person> parsed = CsvHelper.importFromStream(in);
                Context appContext = getApplicationContext();

                viewModel.importPeople(parsed, (insertedPeople, skipped) -> {
                    for (Person p : insertedPeople) {
                        AlarmScheduler.scheduleAll(appContext, p);
                    }
                    runOnUiThread(() -> Toast.makeText(this,
                            "Импортировано: " + insertedPeople.size() + ", пропущено дублей: " + skipped,
                            Toast.LENGTH_LONG).show());
                });
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this,
                        "Ошибка импорта: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}