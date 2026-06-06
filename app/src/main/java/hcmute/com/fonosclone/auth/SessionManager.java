package hcmute.com.fonosclone.auth;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class SessionManager {

    private static final String PREF_NAME = "fonos_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_REGISTERED_NAME = "registered_name";
    private static final String KEY_REGISTERED_EMAIL = "registered_email";
    private static final String KEY_REGISTERED_PASSWORD_HASH = "registered_password_hash";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void registerUser(String name, String email, String password) {
        preferences.edit()
                .putString(KEY_REGISTERED_NAME, name.trim())
                .putString(KEY_REGISTERED_EMAIL, normalizeEmail(email))
                .putString(KEY_REGISTERED_PASSWORD_HASH, hashPassword(password))
                .apply();
    }

    public boolean hasRegisteredUser() {
        return preferences.contains(KEY_REGISTERED_EMAIL)
                && preferences.contains(KEY_REGISTERED_PASSWORD_HASH);
    }

    public boolean canLogin(String email, String password) {
        String savedEmail = preferences.getString(KEY_REGISTERED_EMAIL, "");
        String savedPasswordHash = preferences.getString(KEY_REGISTERED_PASSWORD_HASH, "");

        return savedEmail.equals(normalizeEmail(email))
                && savedPasswordHash.equals(hashPassword(password));
    }

    public void loginRegisteredUser() {
        String name = preferences.getString(KEY_REGISTERED_NAME, "");
        String email = preferences.getString(KEY_REGISTERED_EMAIL, "");
        login(name, email);
    }

    public void login(String name, String email) {
        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_USER_NAME, name.trim())
                .putString(KEY_USER_EMAIL, normalizeEmail(email))
                .apply();
    }

    public void logout() {
        preferences.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_USER_NAME)
                .remove(KEY_USER_EMAIL)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return preferences.getString(KEY_USER_EMAIL, "");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.US);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format(Locale.US, "%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
