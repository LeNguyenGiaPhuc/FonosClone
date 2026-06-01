package hcmute.com.fonosclone;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;

import hcmute.com.fonosclone.data.AppDatabase;

public class ChallengesActivity extends BaseActivity {

    private TextView pointsText;
    private TextView dailySummaryText;
    private TextView weeklySummaryText;
    private LinearLayout dailyContainer;
    private LinearLayout weeklyContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_CHALLENGES);
        setupUserMenu();

        pointsText = findViewById(R.id.tvPoints);
        dailySummaryText = findViewById(R.id.tvDailySummary);
        weeklySummaryText = findViewById(R.id.tvWeeklySummary);
        dailyContainer = findViewById(R.id.dailyMissionsContainer);
        weeklyContainer = findViewById(R.id.weeklyMissionsContainer);

        loadChallengeDashboard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChallengeDashboard();
    }

    private void loadChallengeDashboard() {
        new Thread(() -> {
            ChallengeEngine.ChallengeDashboard dashboard = ChallengeEngine.evaluate(
                    AppDatabase.getInstance(getApplicationContext()).fonosDao()
            );

            runOnUiThread(() -> bindDashboard(dashboard));
        }).start();
    }

    private void bindDashboard(ChallengeEngine.ChallengeDashboard dashboard) {
        pointsText.setText(String.valueOf(dashboard.totalPoints));
        dailySummaryText.setText("Today: " + formatDuration(dashboard.dailySeconds));
        weeklySummaryText.setText("This week: " + formatDuration(dashboard.weeklySeconds));

        renderMissions(dailyContainer, dashboard.dailyMissions);
        renderMissions(weeklyContainer, dashboard.weeklyMissions);
    }

    private void renderMissions(LinearLayout container, List<ChallengeEngine.MissionState> missions) {
        container.removeAllViews();
        for (ChallengeEngine.MissionState mission : missions) {
            container.addView(createMissionCard(mission));
        }
    }

    private MaterialCardView createMissionCard(ChallengeEngine.MissionState mission) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(10));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(dp(2));
        card.setRadius(dp(12));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(14), dp(16), dp(14));

        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(this);
        title.setText(mission.title);
        title.setTextColor(Color.parseColor("#1A1A2E"));
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView points = new TextView(this);
        points.setText("+" + mission.points);
        points.setTextColor(Color.WHITE);
        points.setTextSize(12);
        points.setTypeface(null, Typeface.BOLD);
        points.setPadding(dp(10), dp(4), dp(10), dp(4));
        points.setBackground(makeRoundedDrawable(mission.completed ? "#2E9D55" : "#F07830", dp(12)));
        titleRow.addView(points);
        content.addView(titleRow);

        TextView subtitle = new TextView(this);
        subtitle.setText(mission.subtitle);
        subtitle.setTextColor(Color.parseColor("#7D88A6"));
        subtitle.setTextSize(12);
        subtitle.setPadding(0, dp(4), 0, 0);
        content.addView(subtitle);

        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(mission.target);
        progressBar.setProgress(Math.min(mission.progress, mission.target));
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressTintList(android.content.res.ColorStateList.valueOf(
                    mission.completed ? Color.parseColor("#2E9D55") : Color.parseColor("#F07830")
            ));
        }
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(8)
        );
        progressParams.setMargins(0, dp(12), 0, 0);
        content.addView(progressBar, progressParams);

        TextView status = new TextView(this);
        status.setText(mission.completed
                ? "Completed"
                : formatProgress(mission.progress, mission.target));
        status.setTextColor(mission.completed ? Color.parseColor("#2E9D55") : Color.parseColor("#7D88A6"));
        status.setTextSize(12);
        status.setTypeface(null, mission.completed ? Typeface.BOLD : Typeface.NORMAL);
        status.setPadding(0, dp(8), 0, 0);
        content.addView(status);

        card.addView(content);
        return card;
    }

    private String formatProgress(int progress, int target) {
        if (target <= 5) {
            return progress + "/" + target;
        }
        return formatDuration(progress) + " / " + formatDuration(target);
    }

    private String formatDuration(int seconds) {
        int minutes = Math.max(0, seconds) / 60;
        int remainingSeconds = Math.max(0, seconds) % 60;
        if (minutes > 0) {
            return String.format(Locale.US, "%dm %02ds", minutes, remainingSeconds);
        }
        return remainingSeconds + "s";
    }

    private GradientDrawable makeRoundedDrawable(String color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
