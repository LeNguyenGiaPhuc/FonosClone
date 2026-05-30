package hcmute.com.fonosclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogle;
    TextView tvForgot, tvGoRegister;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if (sessionManager.isLoggedIn()) {
            openHome();
            return;
        }

        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        tvForgot = findViewById(R.id.tvForgot);
        tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> validateLogin());

        btnGoogle.setOnClickListener(v -> {
            sessionManager.login(getString(R.string.google_user_name), getString(R.string.google_user_email));
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            openHome();
        });

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, R.string.forgot_password_message, Toast.LENGTH_SHORT).show()
        );

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void validateLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError(getString(R.string.error_password_min));
            etPassword.requestFocus();
            return;
        }

        if (!sessionManager.hasRegisteredUser()) {
            Toast.makeText(this, R.string.error_no_registered_account, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sessionManager.canLogin(email, password)) {
            Toast.makeText(this, R.string.error_wrong_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.loginRegisteredUser();
        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void openHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
