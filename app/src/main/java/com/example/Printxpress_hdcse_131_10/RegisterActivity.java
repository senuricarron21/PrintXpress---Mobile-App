package com.example.Printxpress_hdcse_131_10;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail, etName, etPassword, etConPassword, etPhone;
    TextView tvBacktologin;
    Button btnRegister2;
    DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etEmail = findViewById(R.id.edtEmail);
        etName = findViewById(R.id.edtName);
        etPhone = findViewById(R.id.edtPhone);
        etPassword = findViewById(R.id.edtPwd);
        etConPassword = findViewById(R.id.edtConPwd);
        btnRegister2 = findViewById(R.id.btRegister);
        tvBacktologin = findViewById(R.id.tvBack);

        db = new DbHelper(this);

        btnRegister2.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String fullName = etName.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String psw = etPassword.getText().toString().trim();
            String conPsw = etConPassword.getText().toString().trim();

            // Validation logic
            if (email.isEmpty() || fullName.isEmpty() || phone.isEmpty() || psw.isEmpty() || conPsw.isEmpty()) {
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!db.validateEmail(email)) {
                etEmail.setError("Invalid Email!");
                etEmail.requestFocus();
                return;
            }

            // Fixed logic: Error if it DOES NOT start with 0 OR length is NOT 10
            if (!phone.startsWith("0") || phone.length() != 10) {
                etPhone.setError("Invalid Phone Number! Must be 10 digits starting with 0");
                return;
            }

            if (!psw.equals(conPsw)) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Register process
            boolean registered = db.registerCustomer(email, fullName, phone, psw);

            if (registered) {
                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.putExtra("email", email); // Pass email to MainActivity
                startActivity(intent);
                finish(); // Close RegisterActivity
            } else {
                Toast.makeText(this, "Registration failed! Email might already be in use.", Toast.LENGTH_SHORT).show();
            }
        });

        tvBacktologin.setOnClickListener(v -> {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
        });
    }
}