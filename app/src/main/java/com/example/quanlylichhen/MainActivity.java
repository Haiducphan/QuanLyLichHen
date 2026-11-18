package com.example.quanlylichhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.quanlylichhen.adapter.AppointmentAdapter;
import com.example.quanlylichhen.database.DatabaseHelper;
import com.example.quanlylichhen.model.Appointment;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextView tvTotalApps, tvTotalReminders, tvEmpty;
    private RecyclerView recyclerView;
    private Button btnAdd;
    private android.widget.ImageView btnLogout;
    private DatabaseHelper databaseHelper;
    private AppointmentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#087F76"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvTotalApps = findViewById(R.id.tvTotalApps);
        tvTotalReminders = findViewById(R.id.tvTotalReminders);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btnAdd);
        btnLogout = findViewById(R.id.btnLogout);

        databaseHelper = new DatabaseHelper(this);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter(new ArrayList<>(), id -> {
            Intent intent = new Intent(MainActivity.this, AppointmentDetailActivity.class);
            intent.putExtra("ID", id);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddAppointmentActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Đăng xuất")
                    .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                    .setPositiveButton("Đăng xuất", (dialog, which) -> {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        ArrayList<Appointment> list = databaseHelper.getAllAppointments();
        adapter.setList(list);
        
        if (list.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        int total = databaseHelper.countAll();
        int reminders = databaseHelper.countReminders();
        
        tvTotalApps.setText(String.valueOf(total));
        tvTotalReminders.setText(String.valueOf(reminders));
    }
}