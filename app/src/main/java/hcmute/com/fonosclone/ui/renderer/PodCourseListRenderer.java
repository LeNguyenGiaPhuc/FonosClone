package hcmute.com.fonosclone.ui.renderer;


import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.PodCourse;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.service.AudioPlayerService;
import hcmute.com.fonosclone.ui.activity.PlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;
import java.util.Locale;


public final class PodCourseListRenderer {

    private PodCourseListRenderer() {
    }

    public static void renderHorizontal(Activity activity, LinearLayout container, List<PodCourse> courses) {
        container.removeAllViews();
        if (courses == null || courses.isEmpty()) {
            return;
        }

        for (int i = 0; i < courses.size(); i++) {
            PodCourse course = courses.get(i);
            MaterialCardView card = createHorizontalCard(activity, course);
            
            // Add margin between cards
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dp(activity, 260),
                    dp(activity, 380)
            );
            if (i < courses.size() - 1) {
                params.setMargins(0, 0, dp(activity, 14), 0);
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            card.setLayoutParams(params);
            container.addView(card);
        }
    }

    public static void renderVertical(Activity activity, LinearLayout container, List<PodCourse> courses) {
        container.removeAllViews();
        if (courses == null || courses.isEmpty()) {
            return;
        }

        for (int i = 0; i < courses.size(); i++) {
            PodCourse course = courses.get(i);
            MaterialCardView card = createVerticalRow(activity, course);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(activity, 80)
            );
            if (i < courses.size() - 1) {
                params.setMargins(0, 0, 0, dp(activity, 10));
            } else {
                params.setMargins(0, 0, 0, 0);
            }
            card.setLayoutParams(params);
            container.addView(card);
        }
    }

    private static MaterialCardView createHorizontalCard(Activity activity, PodCourse course) {
        MaterialCardView card = new MaterialCardView(activity);
        card.setRadius(dp(activity, 16));
        card.setCardElevation(dp(activity, 4));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> playPodCourse(activity, course));

        FrameLayout frameLayout = new FrameLayout(activity);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        // Background Color layout
        int baseColor = Color.parseColor(course.coverColor);
        LinearLayout contentLayout = new LinearLayout(activity);
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        contentLayout.setOrientation(LinearLayout.VERTICAL);
        contentLayout.setBackgroundColor(baseColor);
        contentLayout.setGravity(Gravity.BOTTOM);

        // Top Emoji / Subject Area
        LinearLayout topArea = new LinearLayout(activity);
        LinearLayout.LayoutParams topParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
        );
        topArea.setLayoutParams(topParams);
        topArea.setGravity(Gravity.CENTER);
        
        // Calculate a slightly lighter tone for the top background to add depth
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] = Math.min(1.0f, hsv[2] + 0.1f); // slightly brighter
        topArea.setBackgroundColor(Color.HSVToColor(hsv));

        TextView emojiView = new TextView(activity);
        emojiView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        emojiView.setText(getEmojiForCategory(course.category));
        emojiView.setTextSize(72);
        topArea.addView(emojiView);
        contentLayout.addView(topArea);

        // Bottom Details Area
        LinearLayout bottomArea = new LinearLayout(activity);
        bottomArea.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        bottomArea.setOrientation(LinearLayout.VERTICAL);
        bottomArea.setBackgroundColor(Color.parseColor("#CC000000")); // dark premium semi-transparent overlay
        bottomArea.setPadding(dp(activity, 14), dp(activity, 14), dp(activity, 14), dp(activity, 14));

        // Title
        TextView titleView = new TextView(activity);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titleView.setText(course.title);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setMaxLines(2);
        bottomArea.addView(titleView);

        // Teacher
        TextView teacherView = new TextView(activity);
        LinearLayout.LayoutParams teacherParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        teacherParams.setMargins(0, dp(activity, 5), 0, 0);
        teacherView.setLayoutParams(teacherParams);
        teacherView.setText(course.teacher);
        teacherView.setTextColor(Color.parseColor("#CCCCCC"));
        teacherView.setTextSize(13);
        bottomArea.addView(teacherView);

        // Rating and Badge Row
        LinearLayout ratingRow = new LinearLayout(activity);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        rowParams.setMargins(0, dp(activity, 8), 0, 0);
        ratingRow.setLayoutParams(rowParams);
        ratingRow.setOrientation(LinearLayout.HORIZONTAL);
        ratingRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView ratingView = new TextView(activity);
        ratingView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        ratingView.setText(String.format(Locale.US, "★ %.1f", course.rating));
        ratingView.setTextColor(Color.parseColor("#F5C518"));
        ratingView.setTextSize(12);
        ratingRow.addView(ratingView);

        View divider = new View(activity);
        divider.setLayoutParams(new LinearLayout.LayoutParams(0, 1, 1f));
        ratingRow.addView(divider);

        // Member Badge
        MaterialCardView badgeCard = new MaterialCardView(activity);
        badgeCard.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dp(activity, 24)
        ));
        badgeCard.setRadius(dp(activity, 12));
        badgeCard.setCardBackgroundColor(Color.parseColor("#8B5CF6"));
        badgeCard.setCardElevation(0);

        TextView badgeText = new TextView(activity);
        badgeText.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        badgeText.setText(R.string.member_badge);
        badgeText.setTextColor(Color.WHITE);
        badgeText.setTextSize(11);
        badgeText.setTypeface(null, Typeface.BOLD);
        badgeText.setGravity(Gravity.CENTER);
        badgeText.setPadding(dp(activity, 8), 0, dp(activity, 8), 0);
        badgeCard.addView(badgeText);
        
        ratingRow.addView(badgeCard);
        bottomArea.addView(ratingRow);

        contentLayout.addView(bottomArea);
        frameLayout.addView(contentLayout);

        // Middle Floating Play Button
        MaterialCardView playCard = new MaterialCardView(activity);
        FrameLayout.LayoutParams playParams = new FrameLayout.LayoutParams(
                dp(activity, 52),
                dp(activity, 52)
        );
        playParams.gravity = Gravity.CENTER;
        playParams.setMargins(0, 0, 0, dp(activity, 50)); // Float slightly above the bottom overlay
        playCard.setLayoutParams(playParams);
        playCard.setRadius(dp(activity, 26));
        playCard.setCardBackgroundColor(Color.parseColor("#AAFFFFFF"));
        playCard.setCardElevation(dp(activity, 4));

        ImageView playIcon = new ImageView(activity);
        playIcon.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        playIcon.setImageResource(android.R.drawable.ic_media_play);
        playIcon.setPadding(dp(activity, 14), dp(activity, 12), dp(activity, 10), dp(activity, 12));
        playIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        playIcon.setColorFilter(Color.WHITE);
        playCard.addView(playIcon);

        frameLayout.addView(playCard);
        card.addView(frameLayout);

        return card;
    }

    private static MaterialCardView createVerticalRow(Activity activity, PodCourse course) {
        MaterialCardView card = new MaterialCardView(activity);
        card.setRadius(dp(activity, 12));
        card.setCardElevation(dp(activity, 2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> playPodCourse(activity, course));

        LinearLayout row = new LinearLayout(activity);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12));

        // Left Colored Square Cover
        MaterialCardView coverCard = new MaterialCardView(activity);
        coverCard.setLayoutParams(new LinearLayout.LayoutParams(
                dp(activity, 56),
                dp(activity, 56)
        ));
        coverCard.setRadius(dp(activity, 8));
        coverCard.setCardBackgroundColor(Color.parseColor(course.coverColor));
        coverCard.setCardElevation(0);

        TextView emojiView = new TextView(activity);
        emojiView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        emojiView.setText(getEmojiForCategory(course.category));
        emojiView.setTextSize(24);
        emojiView.setGravity(Gravity.CENTER);
        coverCard.addView(emojiView);
        row.addView(coverCard);

        // Middle Content Column
        LinearLayout textColumn = new LinearLayout(activity);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            );
        textParams.setMargins(dp(activity, 12), 0, dp(activity, 8), 0);
        textColumn.setLayoutParams(textParams);
        textColumn.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(activity);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        titleView.setText(course.title);
        titleView.setTextColor(Color.parseColor("#1A1A2E"));
        titleView.setTextSize(13);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setMaxLines(2);
        textColumn.addView(titleView);

        TextView subtitleView = new TextView(activity);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        subParams.setMargins(0, dp(activity, 3), 0, 0);
        subtitleView.setLayoutParams(subParams);
        subtitleView.setText(String.format(Locale.US, "%s · %.1f ★", course.teacher, course.rating));
        subtitleView.setTextColor(Color.parseColor("#999999"));
        subtitleView.setTextSize(11);
        textColumn.addView(subtitleView);
        row.addView(textColumn);

        // Right Play Arrow
        ImageView playArrow = new ImageView(activity);
        playArrow.setLayoutParams(new LinearLayout.LayoutParams(
                dp(activity, 20),
                dp(activity, 20)
        ));
        playArrow.setImageResource(android.R.drawable.ic_media_play);
        playArrow.setColorFilter(Color.parseColor("#F07030"));
        row.addView(playArrow);

        card.addView(row);
        return card;
    }

    private static String getEmojiForCategory(String category) {
        if (category == null) return "🎓";
        switch (category.toLowerCase()) {
            case "technology":
            case "ai":
                return "🤖";
            case "management":
            case "leadership":
                return "📊";
            case "finance":
                return "💰";
            case "soft skills":
                return "🗣️";
            case "business":
                return "📈";
            case "product":
                return "📦";
            default:
                return "🎓";
        }
    }

    private static void playPodCourse(Activity activity, PodCourse course) {
        // We reuse the PlayerActivity to play PodCourse
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, course.title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, course.teacher);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, "demo_audio");
        intent.putExtra(PlayerActivity.EXTRA_BOOK_ID, course.id + 1000); // offset to prevent collisions with Books in history
        intent.putExtra("cover_image", "book"); // standard drawable fallback cover
        intent.putExtra("cover_color", course.coverColor);
        intent.putExtra("cover_emoji", getEmojiForCategory(course.category));
        activity.startActivity(intent);
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
