package hcmute.com.fonosclone;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
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

    protected static final int TAB_AUDIOBOOKS = 0;
    protected static final int TAB_EBOOKS = 1;
    protected static final int TAB_SUMMARIES = 2;

    private static final int ACTIVE_COLOR = Color.parseColor("#F07030");
    private static final int INACTIVE_COLOR = Color.parseColor("#B0B0B0");

    protected void applySystemBarPadding(int rootViewId) {
        View rootView = findViewById(rootViewId);

        if (rootView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
    }

    protected void setupTabs(int selectedTab) {
        LinearLayout tabAudiobooks = findViewById(R.id.tabAudiobooks);
        LinearLayout tabEbooks = findViewById(R.id.tabEbooks);
        LinearLayout tabSummaries = findViewById(R.id.tabSummaries);

        updateTabUI(R.id.tvTabAudiobooks, R.id.indicatorAudiobooks, selectedTab == TAB_AUDIOBOOKS);
        updateTabUI(R.id.tvTabEbooks, R.id.indicatorEbooks, selectedTab == TAB_EBOOKS);
        updateTabUI(R.id.tvTabSummaries, R.id.indicatorSummaries, selectedTab == TAB_SUMMARIES);

        if (tabAudiobooks != null) {
            tabAudiobooks.setOnClickListener(v -> openTabMenu(selectedTab, TAB_AUDIOBOOKS, MainActivity.class));
        }
        if (tabEbooks != null) {
            tabEbooks.setOnClickListener(v -> openTabMenu(selectedTab, TAB_EBOOKS, EbookActivity.class));
        }
        if (tabSummaries != null) {
            tabSummaries.setOnClickListener(v -> openTabMenu(selectedTab, TAB_SUMMARIES, SummaryActivity.class));
        }
    }

    protected void setupUserMenu() {
        View ivAvatar = findViewById(R.id.ivAvatar);
        if (ivAvatar == null) return;

        ivAvatar.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(this);
            boolean isLoggedIn = sessionManager.isLoggedIn();
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.user_menu, popup.getMenu());

            popup.getMenu().findItem(R.id.menu_login).setVisible(!isLoggedIn);
            popup.getMenu().findItem(R.id.menu_register).setVisible(!isLoggedIn);
            popup.getMenu().findItem(R.id.menu_profile).setVisible(isLoggedIn);
            popup.getMenu().findItem(R.id.menu_logout).setVisible(isLoggedIn);

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.menu_login) {
                    startActivity(new Intent(this, LoginActivity.class));
                    return true;
                } else if (id == R.id.menu_register) {
                    startActivity(new Intent(this, RegisterActivity.class));
                    return true;
                } else if (id == R.id.menu_profile) {
                    startActivity(new Intent(this, ProfileActivity.class));
                    return true;
                } else if (id == R.id.menu_logout) {
                    sessionManager.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void updateTabUI(int tvId, int indicatorId, boolean selected) {
        TextView tv = findViewById(tvId);
        View indicator = findViewById(indicatorId);
        if (tv != null) {
            tv.setTextColor(selected ? Color.WHITE : Color.parseColor("#D6DDFE"));
            tv.setTypeface(null, selected ? Typeface.BOLD : Typeface.NORMAL);
        }
        if (indicator != null) {
            indicator.setBackgroundColor(selected ? Color.parseColor("#F07830") : Color.TRANSPARENT);
        }
    }

    private void openTabMenu(int currentTab, int targetTab, Class<?> targetActivity) {
        if (currentTab == targetTab) return;
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
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

        if (navSach != null) navSach.setOnClickListener(v -> openTab(selectedItem, NAV_BOOKS, MainActivity.class));
        if (navPodCourse != null) navPodCourse.setOnClickListener(v -> openTab(selectedItem, NAV_PODCOURSE, PodcourseActivity.class));
        if (navKhamPha != null) navKhamPha.setOnClickListener(v -> openTab(selectedItem, NAV_EXPLORE, SearchActivity.class));
        if (navThuThach != null) navThuThach.setOnClickListener(v -> openTab(selectedItem, NAV_CHALLENGES, ChallengesActivity.class));
        if (navThuVien != null) navThuVien.setOnClickListener(v -> openTab(selectedItem, NAV_LIBRARY, LibraryActivity.class));
    }

    private void openTab(int currentItem, int targetItem, Class<?> targetActivity) {
        if (currentItem == targetItem) return;

        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void setNavSelected(View view, boolean selected) {
        if (view == null) return;

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
