package hcmute.com.fonosclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogle;
    TextView tvForgot, tvGoRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail       = findViewById(R.id.etEmail);
        etPassword    = findViewById(R.id.etPassword);
        btnLogin      = findViewById(R.id.btnLogin);
        btnGoogle     = findViewById(R.id.btnGoogle);
        tvForgot      = findViewById(R.id.tvForgot);
        tvGoRegister  = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> validateLogin());

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập Google...", Toast.LENGTH_SHORT).show()
        );

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, "Vui lòng kiểm tra email để đặt lại mật khẩu", Toast.LENGTH_SHORT).show()
        );

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void validateLogin() {
        String email    = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "✅ Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
    }
}