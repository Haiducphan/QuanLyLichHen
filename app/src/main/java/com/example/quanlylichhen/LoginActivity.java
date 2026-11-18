
package com.example.quanlylichhen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.quanlylichhen.database.DatabaseHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsername;
    private EditText edtPassword;
    private TextView tvLoginError;
    private AppCompatButton btnLogin;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#F3F8F7"));

        // Xu ly hien thi an toan voi thanh trang thai
        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (view, insets) -> {
                    Insets bars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );
                    view.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                    return insets;
                }
        );

        // Anh xa giao dien
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        tvLoginError = findViewById(R.id.tvLoginError);
        btnLogin = findViewById(R.id.btnLogin);

        // Khoi tao DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        // Bat su kien dang nhap
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {

        // Lay thong tin nguoi dung
        String username = edtUsername.getText()
                .toString().trim();

        String password = edtPassword.getText()
                .toString();

        // An loi cu
        tvLoginError.setVisibility(View.GONE);

        // Kiem tra ten dang nhap
        if (username.isEmpty()) {

            edtUsername.setError(
                    "Vui lòng nhập tên đăng nhập"
            );

            edtUsername.requestFocus();
            return;
        }

        // Kiem tra mat khau
        if (password.trim().isEmpty()) {

            edtPassword.setError(
                    "Vui lòng nhập mật khẩu"
            );

            edtPassword.requestFocus();
            return;
        }

        // Kiem tra tai khoan bang SQLite
        boolean isValid = databaseHelper.checkLogin(
                username,
                password
        );

        if (isValid) {

            // Dang nhap thanh cong
            Intent intent = new Intent(
                    LoginActivity.this,
                    MainActivity.class
            );

            startActivity(intent);

            // Khong quay lai LoginActivity khi nhan Back
            finish();

        } else {

            // Dang nhap that bai
            tvLoginError.setText(
                    "Sai tên đăng nhập hoặc mật khẩu"
            );

            tvLoginError.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {

        if (databaseHelper != null) {
            databaseHelper.close();
        }

        super.onDestroy();
    }
}
