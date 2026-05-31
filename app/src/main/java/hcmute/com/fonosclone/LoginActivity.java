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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 1001;
    private static final String GOOGLE_WEB_CLIENT_PLACEHOLDER = "REPLACE_WITH_FIREBASE_WEB_CLIENT_ID";

    EditText etEmail, etPassword;
    Button btnLogin, btnGoogle;
    TextView tvForgot, tvGoRegister;
    SessionManager sessionManager;
    GoogleSignInClient googleSignInClient;

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

        setupGoogleSignIn();

        btnLogin.setOnClickListener(v -> validateLogin());

        btnGoogle.setOnClickListener(v -> startGoogleSignIn());

        tvForgot.setOnClickListener(v ->
                Toast.makeText(this, R.string.forgot_password_message, Toast.LENGTH_SHORT).show()
        );

        tvGoRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });
    }

    private void setupGoogleSignIn() {
        String webClientId = getString(R.string.google_web_client_id);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    private void startGoogleSignIn() {
        String webClientId = getString(R.string.google_web_client_id);
        if (webClientId.equals(GOOGLE_WEB_CLIENT_PLACEHOLDER)) {
            Toast.makeText(this, R.string.google_sign_in_missing_config, Toast.LENGTH_LONG).show();
            return;
        }

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != RC_GOOGLE_SIGN_IN) return;

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            if (account == null || account.getIdToken() == null) {
                Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show();
                return;
            }
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(this, getString(R.string.google_sign_in_failed) + " " + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng nhập Google...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null) {
                            progressDialog.dismiss();
                            Toast.makeText(this, R.string.google_sign_in_failed, Toast.LENGTH_LONG).show();
                            return;
                        }
                        saveGoogleUserSession(user, progressDialog);
                    } else {
                        progressDialog.dismiss();
                        String error = task.getException() != null
                                ? task.getException().getLocalizedMessage()
                                : getString(R.string.google_sign_in_failed);
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveGoogleUserSession(FirebaseUser user, ProgressDialog progressDialog) {
        String name = user.getDisplayName();
        String email = user.getEmail();

        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.google_user_name);
        }
        if (email == null || email.trim().isEmpty()) {
            email = getString(R.string.google_user_email);
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("name", name);
        userData.put("email", email);
        userData.put("provider", "google");
        userData.put("updatedAt", System.currentTimeMillis());
        if (user.getPhotoUrl() != null) {
            userData.put("avatarUrl", user.getPhotoUrl().toString());
        }

        String finalName = name;
        String finalEmail = email;
        FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid())
                .set(userData, SetOptions.merge())
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (!task.isSuccessful()) {
                        String error = task.getException() != null
                                ? task.getException().getLocalizedMessage()
                                : getString(R.string.google_sign_in_failed);
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        return;
                    }

                    sessionManager.login(finalName, finalEmail);
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
                    openHome();
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
