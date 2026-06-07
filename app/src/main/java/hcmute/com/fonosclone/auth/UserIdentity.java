package hcmute.com.fonosclone.auth;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public final class UserIdentity {
    public static final String GUEST_USER_ID = "guest";

    private UserIdentity() {
    }

    public static String getCurrentUserId(Context context) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && !isBlank(firebaseUser.getUid())) {
            return firebaseUser.getUid();
        }

        SessionManager sessionManager = new SessionManager(context);
        String email = sessionManager.getUserEmail();
        if (!isBlank(email)) {
            return "local_" + sha256(email.trim().toLowerCase(Locale.US));
        }

        return GUEST_USER_ID;
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format(Locale.US, "%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            return Integer.toHexString(value.hashCode());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
