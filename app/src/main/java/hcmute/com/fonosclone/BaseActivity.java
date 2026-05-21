package hcmute.com.fonosclone;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BaseActivity extends AppCompatActivity {

    protected static final int NAV_BOOKS = 0;
    protected static final int NAV_PODCOURSE = 1;
    protected static final int NAV_EXPLORE = 2;
    protected static final int NAV_CHALLENGES = 3;
    protected static final int NAV_LIBRARY = 4;

    private static final int ACTIVE_COLOR = Color.parseColor("#F07030");
    private static final int INACTIVE_COLOR = Color.parseColor("#B0B0B0");

    protected void applySystemBarPadding(int rootViewId) {
        View rootView = findViewById(rootViewId);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    protected void setupBottomNavigation(int selectedItem) {
        LinearLayout navSach = findViewById(R.id.navSach);
        LinearLayout navPodCourse = findViewById(R.id.navPodCourse);
        LinearLayout navKhamPha = findViewById(R.id.navKhamPha);
        LinearLayout navThuThach = findViewById(R.id.navThuThach);
        LinearLayout navThuVien = findViewById(R.id.navThuVien);

        setNavSelected(navSach, selectedItem == NAV_BOOKS);
        setNavSelected(navPodCourse, selectedItem == NAV_PODCOURSE);
        setNavSelected(navKhamPha, selectedItem == NAV_EXPLORE);
        setNavSelected(navThuThach, selectedItem == NAV_CHALLENGES);
        setNavSelected(navThuVien, selectedItem == NAV_LIBRARY);

        navSach.setOnClickListener(v -> openTab(selectedItem, NAV_BOOKS, MainActivity.class));
        navPodCourse.setOnClickListener(v -> openTab(selectedItem, NAV_PODCOURSE, PodcourseActivity.class));
        navKhamPha.setOnClickListener(v -> openTab(selectedItem, NAV_EXPLORE, SearchActivity.class));
        navThuThach.setOnClickListener(v -> openTab(selectedItem, NAV_CHALLENGES, ChallengesActivity.class));
        navThuVien.setOnClickListener(v -> openTab(selectedItem, NAV_LIBRARY, LibraryActivity.class));
    }

    private void openTab(int currentItem, int targetItem, Class<?> targetActivity) {
        if (currentItem == targetItem) return;

        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void setNavSelected(View view, boolean selected) {
        int color = selected ? ACTIVE_COLOR : INACTIVE_COLOR;

        if (view instanceof ImageView) {
            ((ImageView) view).setColorFilter(color);
        }

        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setTextColor(color);
            textView.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setNavSelected(group.getChildAt(i), selected);
            }
        }
    }
}
