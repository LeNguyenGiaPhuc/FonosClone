package hcmute.com.fonosclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    CheckBox cbTerms;
    Button btnRegister;
    TextView tvGoLogin, tvTerms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName        = findViewById(R.id.etFullName);
        etEmail           = findViewById(R.id.etEmail);
        etPassword        = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms           = findViewById(R.id.cbTerms);
        btnRegister       = findViewById(R.id.btnRegister);
        tvGoLogin         = findViewById(R.id.tvGoLogin);
        tvTerms           = findViewById(R.id.tvTerms);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> validateRegister());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        tvTerms.setOnClickListener(v ->
                Toast.makeText(this, "Điều khoản dịch vụ", Toast.LENGTH_SHORT).show()
        );
    }

    private void validateRegister() {
        String name     = etFullName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String pass     = etPassword.getText().toString().trim();
        String confirm  = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError("Vui lòng nhập họ tên");
            etFullName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Email không hợp lệ");
            etEmail.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError("Mật khẩu tối thiểu 6 ký tự");
            etPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            etConfirmPassword.setError("Mật khẩu không khớp");
            etConfirmPassword.requestFocus();
            return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng đồng ý điều khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "🎉 Đăng ký thành công! Chào " + name, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}
