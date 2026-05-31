package hcmute.com.fonosclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // Show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Sign in via Firebase Auth
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Fetch user's profile metadata from Firestore to sync their real display name
                            FirebaseFirestore.getInstance().collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(docTask -> {
                                        progressDialog.dismiss();
                                        String name = user.getDisplayName();
                                        if (docTask.isSuccessful() && docTask.getResult() != null && docTask.getResult().exists()) {
                                            String firestoreName = docTask.getResult().getString("name");
                                            if (firestoreName != null && !firestoreName.isEmpty()) {
                                                name = firestoreName;
                                            }
                                        }
                                        if (name == null || name.isEmpty()) {
                                            name = "Thành viên Fonos";
                                        }
                                        // Cache session locally in SessionManager for fast offline check
                                        sessionManager.login(name, email);
                                        Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                                        openHome();
                                    });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Đã xảy ra lỗi hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        String error = task.getException() != null ? task.getException().getLocalizedMessage() : "Đăng nhập thất bại";
                        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openHome() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
