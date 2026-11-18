package com.example.quanlylichhen;

import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.quanlylichhen.database.DatabaseHelper;
import com.example.quanlylichhen.model.Appointment;
import java.util.Calendar;

public class AddAppointmentActivity extends AppCompatActivity {
    private EditText edtTitle, edtLocation;
    private DatePicker datePicker;
    private TimePicker timePicker;
    private SwitchCompat swReminder;
    private Button btnSave;
    private ImageView btnBack;
    private TextView tvHeaderTitle;
    private DatabaseHelper databaseHelper;

    private int appointmentId = -1;
    private boolean isEditMode = false;
    private String initialTitle = "";
    private String initialLocation = "";
    private String initialDate = "";
    private String initialTime = "";
    private boolean initialReminder = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_appointment);

        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#087F76"));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        edtTitle = findViewById(R.id.edtTitle);
        edtLocation = findViewById(R.id.edtLocation);
        datePicker = findViewById(R.id.datePicker);
        timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        swReminder = findViewById(R.id.swReminder);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        databaseHelper = new DatabaseHelper(this);

        appointmentId = getIntent().getIntExtra("EXTRA_APPOINTMENT_ID", -1);
        if (appointmentId != -1) {
            isEditMode = true;
            if (tvHeaderTitle != null) {
                tvHeaderTitle.setText("Chỉnh sửa lịch hẹn");
            }
            btnSave.setText("Lưu thay đổi");

            Appointment appt = databaseHelper.getAppointmentById(appointmentId);
            if (appt != null) {
                initialTitle = appt.getTitle();
                initialLocation = appt.getLocation();
                initialDate = appt.getDate();
                initialTime = appt.getTime();
                initialReminder = appt.isReminder();

                edtTitle.setText(initialTitle);
                edtLocation.setText(initialLocation);
                swReminder.setChecked(initialReminder);

                try {
                    String[] d = initialDate.split("-");
                    int y = Integer.parseInt(d[0]);
                    int m = Integer.parseInt(d[1]) - 1;
                    int day = Integer.parseInt(d[2]);
                    datePicker.updateDate(y, m, day);
                } catch (Exception ignored) {}

                try {
                    String[] t = initialTime.split(":");
                    int h = Integer.parseInt(t[0]);
                    int min = Integer.parseInt(t[1]);
                    timePicker.setHour(h);
                    timePicker.setMinute(min);
                } catch (Exception ignored) {}
            }
        }

        btnSave.setOnClickListener(v -> saveAppointment());
        btnBack.setOnClickListener(v -> handleBack());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });
    }

    private void handleBack() {
        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();
        String currentDateStr = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        String currentTimeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute);

        boolean isChanged;
        if (isEditMode) {
            isChanged = !edtTitle.getText().toString().trim().equals(initialTitle) ||
                        !edtLocation.getText().toString().trim().equals(initialLocation) ||
                        !currentDateStr.equals(initialDate) ||
                        !currentTimeStr.equals(initialTime) ||
                        swReminder.isChecked() != initialReminder;
        } else {
            isChanged = !edtTitle.getText().toString().trim().isEmpty() ||
                        !edtLocation.getText().toString().trim().isEmpty();
        }

        if (isChanged) {
            new AlertDialog.Builder(this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có thay đổi chưa lưu. Bạn có chắc chắn muốn thoát?")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Hủy", null)
                .show();
        } else {
            finish();
        }
    }

    private void saveAppointment() {
        String title = edtTitle.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();

        if (title.isEmpty()) {
            edtTitle.setError("Vui lòng nhập tên lịch hẹn");
            edtTitle.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            edtLocation.setError("Vui lòng nhập địa điểm");
            edtLocation.requestFocus();
            return;
        }

        int year = datePicker.getYear();
        int month = datePicker.getMonth();
        int day = datePicker.getDayOfMonth();

        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        String dateStr = String.format(java.util.Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
        String timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute);

        // Kiem tra thoi gian trong tuong lai
        boolean needCheckFuture = !isEditMode || (!dateStr.equals(initialDate) || !timeStr.equals(initialTime));
        if (needCheckFuture) {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, day, hour, minute, 0);

            if (selectedCal.before(Calendar.getInstance())) {
                Toast.makeText(this, "Thời điểm hẹn phải sau hiện tại", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnSave.setEnabled(false); // Ngan nhan nhieu lan

        if (isEditMode) {
            Appointment appt = new Appointment(appointmentId, title, location, dateStr, timeStr, swReminder.isChecked());
            boolean success = databaseHelper.updateAppointment(appt);
            if (success) {
                Toast.makeText(this, "Cập nhật lịch hẹn thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật lịch hẹn", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            }
        } else {
            Appointment appt = new Appointment(0, title, location, dateStr, timeStr, swReminder.isChecked());
            long id = databaseHelper.insertAppointment(appt);
            if (id > 0) {
                Toast.makeText(this, "Thêm lịch hẹn thành công", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi thêm lịch hẹn", Toast.LENGTH_SHORT).show();
                btnSave.setEnabled(true);
            }
        }
    }
}