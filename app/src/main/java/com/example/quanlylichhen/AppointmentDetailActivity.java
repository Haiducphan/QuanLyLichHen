package com.example.quanlylichhen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.SwitchCompat;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.quanlylichhen.database.DatabaseHelper;
import com.example.quanlylichhen.model.Appointment;

public class AppointmentDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvLocation, tvDateTime, tvReminderStatus;
    private SwitchCompat swReminder;
    private Button btnDelete, btnBackSpecific, btnEdit;
    private ImageView btnBack;
    
    private DatabaseHelper databaseHelper;
    private Appointment appointment;
    private int appointmentId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_appointment_detail);
        
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#087F76"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvTitle = findViewById(R.id.tvTitle);
        tvLocation = findViewById(R.id.tvLocation);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvReminderStatus = findViewById(R.id.tvReminderStatus);
        swReminder = findViewById(R.id.swReminder);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);
        btnBackSpecific = findViewById(R.id.btnBackSpecific);
        btnEdit = findViewById(R.id.btnEdit);

        databaseHelper = new DatabaseHelper(this);
        
        appointmentId = getIntent().getIntExtra("ID", -1);
        if (appointmentId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy lịch hẹn", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnBack.setOnClickListener(v -> finish());
        btnBackSpecific.setOnClickListener(v -> finish());
        
        btnEdit.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(AppointmentDetailActivity.this, AddAppointmentActivity.class);
            intent.putExtra("EXTRA_APPOINTMENT_ID", appointmentId);
            startActivity(intent);
        });
        
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Xóa lịch hẹn")
                .setMessage("Bạn có chắc chắn muốn xóa lịch hẹn này?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    boolean success = databaseHelper.deleteAppointment(appointmentId);
                    if (success) {
                        Toast.makeText(this, "Đã xóa lịch hẹn", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi xóa", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
        });
        
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (appointment != null && appointment.isReminder() != isChecked) {
                boolean success = databaseHelper.updateReminder(appointmentId, isChecked);
                if (success) {
                    appointment.setReminder(isChecked);
                    updateReminderText();
                } else {
                    swReminder.setChecked(!isChecked); // revert
                    Toast.makeText(this, "Lỗi khi cập nhật nhắc nhở", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        appointment = databaseHelper.getAppointmentById(appointmentId);
        if (appointment == null) {
            Toast.makeText(this, "Lịch hẹn đã bị xóa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvTitle.setText(appointment.getTitle());
        tvLocation.setText(appointment.getLocation());
        
        String date = appointment.getDate();
        try {
            String[] parts = date.split("-");
            date = parts[2] + "/" + parts[1] + "/" + parts[0];
        } catch (Exception e) {}
        
        tvDateTime.setText(date + " · " + appointment.getTime());
        
        swReminder.setOnCheckedChangeListener(null);
        swReminder.setChecked(appointment.isReminder());
        
        swReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (appointment != null) {
                boolean success = databaseHelper.updateReminder(appointmentId, isChecked);
                if (success) {
                    appointment.setReminder(isChecked);
                    updateReminderText();
                } else {
                    swReminder.setChecked(!isChecked); // revert
                    Toast.makeText(AppointmentDetailActivity.this, "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        updateReminderText();
    }
    
    private void updateReminderText() {
        if (swReminder.isChecked()) {
            tvReminderStatus.setText("Đang bật");
            tvReminderStatus.setTextColor(0xFF087F76);
        } else {
            tvReminderStatus.setText("Đang tắt");
            tvReminderStatus.setTextColor(0xFF78908B);
        }
    }
}