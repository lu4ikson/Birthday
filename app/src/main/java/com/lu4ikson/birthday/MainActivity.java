package com.lu4ikson.birthday;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {

    private final androidx.activity.result.ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.RequestPermission(), granted -> {
                // Можно показать Toast, если хочешь объяснить пользователю, но не обязательно
            });
    private PersonViewModel viewModel;
    private PersonAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NotificationHelper.createChannel(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        //Разрешить установку будильников и напоминаний
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.app.AlarmManager alarmManager = getSystemService(android.app.AlarmManager.class);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerViewUpcoming);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PersonAdapter();
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PersonViewModel.class);
        viewModel.getAllPeople().observe(this, people -> {
            adapter.setItems(BirthdayUtils.getUpcoming(people));
        });

        findViewById(R.id.buttonAdd).setOnClickListener(v ->
                new AddPersonDialogFragment().show(getSupportFragmentManager(), "add_person"));

        findViewById(R.id.buttonFullList).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, FullListActivity.class));
        });
    }
}