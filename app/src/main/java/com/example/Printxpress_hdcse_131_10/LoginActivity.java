package com.example.Printxpress_hdcse_131_10;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin, btnRegister1;
    DbHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.edtemail);
        edtPassword = findViewById(R.id.edtpassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister1 = findViewById(R.id.btnRegister);

        db = new DbHelper(this);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty. Enter the email and password",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Calls the newly added validateUser method
            boolean isMatch = db.validateUser(email, password);

            if (isMatch) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("email", email);

                if (getIntent().getBooleanExtra("redirectToCart", false)) {
                    intent.putExtra("openCart", true);
                }

                startActivity(intent);
                finish(); // Prevents going back to login screen on back button
            } else {
                Toast.makeText(this, "Login Unsuccessful. Please Enter Correct Credentials.",
                        Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister1.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}