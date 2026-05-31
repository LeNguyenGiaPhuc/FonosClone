package hcmute.com.fonosclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etFullName, etEmail, etPassword, etConfirmPassword;
    CheckBox cbTerms;
    Button btnRegister;
    TextView tvGoLogin, tvTerms;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);
        tvTerms = findViewById(R.id.tvTerms);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> validateRegister());

        tvGoLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        tvTerms.setOnClickListener(v ->
                Toast.makeText(this, R.string.terms_message, Toast.LENGTH_SHORT).show()
        );
    }

    private void validateRegister() {
        String name = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (name.isEmpty()) {
            etFullName.setError(getString(R.string.error_empty_name));
            etFullName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(getString(R.string.error_invalid_email));
            etEmail.requestFocus();
            return;
        }
        if (pass.length() < 6) {
            etPassword.setError(getString(R.string.error_password_min));
            etPassword.requestFocus();
            return;
        }
        if (!pass.equals(confirm)) {
            etConfirmPassword.setError(getString(R.string.error_password_mismatch));
            etConfirmPassword.requestFocus();
            return;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, R.string.error_terms_required, Toast.LENGTH_SHORT).show();
            return;
        }

        // Show progress dialog during registration
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng ký tài khoản...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Register user via Firebase Auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            // Update display name in Firebase Profile
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                // Save profile metadata in Firestore users collection
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("uid", user.getUid());
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("createdAt", System.currentTimeMillis());

                                FirebaseFirestore.getInstance().collection("users")
                                        .document(user.getUid())
                                        .set(userData)
                                        .addOnCompleteListener(firestoreTask -> {
                                            progressDialog.dismiss();
                                            // Cache session in SessionManager for local speed
                                            sessionManager.login(name, email);
                                            Toast.makeText(RegisterActivity.this, getString(R.string.register_success, name), Toast.LENGTH_LONG).show();
                                            
                                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            finish();
                                        });
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Đã xảy ra lỗi hệ thống!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        progressDialog.dismiss();
                        String error = task.getException() != null ? task.getException().getLocalizedMessage() : "Đăng ký thất bại";
                        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
