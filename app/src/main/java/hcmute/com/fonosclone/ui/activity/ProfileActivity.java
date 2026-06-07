package hcmute.com.fonosclone.ui.activity;


import hcmute.com.fonosclone.auth.SessionManager;
import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.R;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;


public class ProfileActivity extends BaseActivity {

    private static final int LEVEL_TARGET_SECONDS = 3600;

    private TextView nameText;
    private TextView handleText;
    private TextView joinedText;
    private TextView listenedSecondsText;
    private ProgressBar levelProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        applySystemBarPadding(R.id.main);

        nameText = findViewById(R.id.tvProfileName);
        handleText = findViewById(R.id.tvProfileHandle);
        joinedText = findViewById(R.id.tvProfileJoined);
        listenedSecondsText = findViewById(R.id.tvListenedSeconds);
        levelProgress = findViewById(R.id.pbProfileLevel);

        findViewById(R.id.btnProfileBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnProfileMenu).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );

        bindUserInfo();
        loadListeningStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListeningStats();
    }

    private void bindUserInfo() {
        SessionManager sessionManager = new SessionManager(this);
        String name = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();

        if (name == null || name.trim().isEmpty()) {
            name = getString(R.string.profile_guest_name);
        }
        if (email == null || email.trim().isEmpty()) {
            email = getString(R.string.profile_guest_email);
        }

        nameText.setText(name);
        handleText.setText(getString(R.string.profile_handle_format, email));
        joinedText.setText(getString(R.string.profile_joined));
    }

    private void loadListeningStats() {
        new Thread(() -> {
            int listenedSeconds = AppDatabase
                    .getInstance(getApplicationContext())
                    .fonosDao()
                    .getTotalListenedSeconds(UserIdentity.getCurrentUserId(getApplicationContext()));
            int progress = Math.min(100, listenedSeconds * 100 / LEVEL_TARGET_SECONDS);

            runOnUiThread(() -> {
                listenedSecondsText.setText(String.valueOf(listenedSeconds));
                levelProgress.setProgress(progress);
            });
        }).start();
    }
}
