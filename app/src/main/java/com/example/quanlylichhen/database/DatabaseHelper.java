
package com.example.quanlylichhen.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.quanlylichhen.model.Appointment;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "appointments.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_APPOINTMENTS = "appointments";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Tạo cơ sở dữ liệu lần đầu
    @Override
    public void onCreate(SQLiteDatabase db) {

        String createUsers = "CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL)";

        db.execSQL(createUsers);

        String createAppointments = "CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "location TEXT NOT NULL, " +
                "date TEXT NOT NULL, " +
                "time TEXT NOT NULL, " +
                "reminder INTEGER NOT NULL)";

        db.execSQL(createAppointments);

        // Tài khoản đăng nhập mẫu
        insertUser(db, "admin", "123456");

        // Ba lịch hẹn mẫu theo đề bài
        insertAppointment(db, new Appointment(
                0, "Họp nhóm đồ án", "Phòng B203",
                "2026-10-25", "09:00", true));

        insertAppointment(db, new Appointment(
                0, "Khám răng", "Nha khoa Minh Tâm",
                "2026-10-26", "14:30", true));

        insertAppointment(db, new Appointment(
                0, "Đi cà phê với bạn", "Highland Coffee",
                "2026-10-27", "19:00", false));
    }

    // Xử lý khi tăng phiên bản cơ sở dữ liệu
    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS appointments");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // 1. Kiểm tra đăng nhập
    public boolean checkLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE username = ? AND password = ?",
                new String[]{username, password}
        );

        try {
            return cursor.moveToFirst();
        } finally {
            cursor.close();
        }
    }

    // 2. Thêm tài khoản
    public long insertUser(String username, String password) {
        return insertUser(getWritableDatabase(), username, password);
    }

    private long insertUser(SQLiteDatabase db,
                            String username, String password) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);

        return db.insert(TABLE_USERS, null, values);
    }

    // 3. Thêm lịch hẹn
    public long insertAppointment(Appointment appointment) {
        return insertAppointment(getWritableDatabase(), appointment);
    }

    private long insertAppointment(SQLiteDatabase db,
                                   Appointment appointment) {
        ContentValues values = new ContentValues();
        values.put("title", appointment.getTitle());
        values.put("location", appointment.getLocation());
        values.put("date", appointment.getDate());
        values.put("time", appointment.getTime());
        values.put("reminder", appointment.isReminder() ? 1 : 0);

        return db.insert(TABLE_APPOINTMENTS, null, values);
    }

    // Chuyển dữ liệu Cursor thành đối tượng Appointment
    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment();

        appointment.setId(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        appointment.setTitle(
                cursor.getString(cursor.getColumnIndexOrThrow("title")));
        appointment.setLocation(
                cursor.getString(cursor.getColumnIndexOrThrow("location")));
        appointment.setDate(
                cursor.getString(cursor.getColumnIndexOrThrow("date")));
        appointment.setTime(
                cursor.getString(cursor.getColumnIndexOrThrow("time")));
        appointment.setReminder(
                cursor.getInt(cursor.getColumnIndexOrThrow("reminder")) == 1);

        return appointment;
    }

    // 4. Lấy danh sách lịch hẹn theo thứ tự thời gian
    public ArrayList<Appointment> getAllAppointments() {
        ArrayList<Appointment> list = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_APPOINTMENTS,
                null,
                null,
                null,
                null,
                null,
                "date ASC, time ASC"
        );

        try {
            while (cursor.moveToNext()) {
                list.add(cursorToAppointment(cursor));
            }
        } finally {
            cursor.close();
        }

        return list;
    }

    // 5. Lấy lịch hẹn theo ID
    public Appointment getAppointmentById(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_APPOINTMENTS,
                null,
                "id = ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null
        );

        try {
            if (cursor.moveToFirst()) {
                return cursorToAppointment(cursor);
            }
        } finally {
            cursor.close();
        }

        return null;
    }

    // 6. Bật hoặc tắt nhắc nhở
    public boolean updateReminder(int id, boolean on) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("reminder", on ? 1 : 0);

        int rows = db.update(
                TABLE_APPOINTMENTS,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return rows > 0;
    }

    // 7. Xóa lịch hẹn
    public boolean deleteAppointment(int id) {
        SQLiteDatabase db = getWritableDatabase();

        int rows = db.delete(
                TABLE_APPOINTMENTS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return rows > 0;
    }

    // 10. Cập nhật toàn bộ thông tin lịch hẹn
    public boolean updateAppointment(Appointment appointment) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("title", appointment.getTitle());
        values.put("location", appointment.getLocation());
        values.put("date", appointment.getDate());
        values.put("time", appointment.getTime());
        values.put("reminder", appointment.isReminder() ? 1 : 0);

        int rows = db.update(
                TABLE_APPOINTMENTS,
                values,
                "id = ?",
                new String[]{String.valueOf(appointment.getId())}
        );

        return rows > 0;
    }

    // 8. Đếm tổng lịch hẹn
    public int countAll() {
        return countQuery(
                "SELECT COUNT(*) FROM appointments",
                null
        );
    }

    // 9. Đếm lịch hẹn bật nhắc nhở
    public int countReminders() {
        return countQuery(
                "SELECT COUNT(*) FROM appointments WHERE reminder = ?",
                new String[]{"1"}
        );
    }

    private int countQuery(String sql, String[] args) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(sql, args);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        } finally {
            cursor.close();
        }
    }
}
