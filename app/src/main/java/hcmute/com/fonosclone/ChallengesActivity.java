package hcmute.com.fonosclone;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import hcmute.com.fonosclone.data.AppDatabase;

public class ChallengesActivity extends BaseActivity {

    private static final int DAILY_TARGET_SECONDS = 30;

    private ProgressBar dailyProgressBar;
    private TextView missionProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_CHALLENGES);
        setupUserMenu();

        dailyProgressBar = findViewById(R.id.pbDailyMission);
        missionProgressText = findViewById(R.id.tvMissionProgress);
        loadListeningProgress();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadListeningProgress();
    }

    private void loadListeningProgress() {
        new Thread(() -> {
            int listenedSeconds = AppDatabase
                    .getInstance(getApplicationContext())
                    .fonosDao()
                    .getTotalListenedSeconds();
            int cappedSeconds = Math.min(listenedSeconds, DAILY_TARGET_SECONDS);

            runOnUiThread(() -> {
                dailyProgressBar.setMax(DAILY_TARGET_SECONDS);
                dailyProgressBar.setProgress(cappedSeconds);
                missionProgressText.setText(getString(
                        R.string.mission_progress_format,
                        cappedSeconds,
                        DAILY_TARGET_SECONDS
                ));
            });
        }).start();
    }
}
