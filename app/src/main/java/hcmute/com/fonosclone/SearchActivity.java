package hcmute.com.fonosclone;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.SeedData;

public class SearchActivity extends BaseActivity {

    private static final String[] CATEGORY_COLORS = {
            "#7A4A2E", "#4B3B96", "#3E7B3C", "#A7444C",
            "#946B00", "#2D8C92", "#315F9E", "#8E4C78",
            "#506A35", "#8A5A2B"
    };

    private static final int[] CATEGORY_ICONS = {
            android.R.drawable.ic_media_play,
            android.R.drawable.ic_menu_upload,
            android.R.drawable.ic_menu_agenda,
            android.R.drawable.ic_menu_gallery,
            android.R.drawable.ic_menu_edit,
            android.R.drawable.ic_menu_compass,
            android.R.drawable.ic_menu_search,
            android.R.drawable.ic_menu_sort_by_size,
            android.R.drawable.ic_menu_manage,
            android.R.drawable.ic_menu_info_details
    };

    private EditText etSearch;
    private LinearLayout defaultSearchContent;
    private LinearLayout resultsContainer;
    private LinearLayout searchBookListContainer;
    private LinearLayout categoryGridContainer;
    private TextView tvResultsTitle;
    private FonosDao dao;
    private FonosRepository repository;

    private boolean isProgrammaticChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_EXPLORE);
        setupUserMenu();

        dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
        repository = new FonosRepository(this);

        etSearch = findViewById(R.id.etSearch);
        defaultSearchContent = findViewById(R.id.defaultSearchContent);
        resultsContainer = findViewById(R.id.resultsContainer);
        searchBookListContainer = findViewById(R.id.searchBookListContainer);
        categoryGridContainer = findViewById(R.id.categoryGridContainer);
        tvResultsTitle = findViewById(R.id.tvResultsTitle);

        setupCategoryClickListeners();
        setupSearchInputListener();

        findViewById(R.id.btnBackToCategories).setOnClickListener(v -> showDefaultCategories());
    }

    private void setupCategoryClickListeners() {
        loadCategoriesFromDatabase();
    }

    private void loadCategoriesFromDatabase() {
        new Thread(() -> {
            if (dao.countBooks() == 0) {
                SeedData.insertSampleData(dao);
            }

            LinkedHashMap<String, Integer> categoryCounts = new LinkedHashMap<>();
            List<Book> allBooks = dao.getAllBooks();
            for (Book book : allBooks) {
                String category = normalizeCategory(book.category);
                if (category.isEmpty()) continue;

                Integer currentCount = categoryCounts.get(category);
                categoryCounts.put(category, currentCount == null ? 1 : currentCount + 1);
            }

            List<CategoryItem> categories = new ArrayList<>();
            int index = 0;
            for (Map.Entry<String, Integer> entry : categoryCounts.entrySet()) {
                categories.add(new CategoryItem(
                        entry.getKey(),
                        entry.getValue(),
                        getCategoryColor(index),
                        getCategoryIcon(index)
                ));
                index++;
            }

            runOnUiThread(() -> renderCategoryGrid(categories));
        }).start();
    }

    private void renderCategoryGrid(List<CategoryItem> categories) {
        if (categoryGridContainer == null) return;

        categoryGridContainer.removeAllViews();
        if (categories == null || categories.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText(R.string.no_books_yet);
            emptyView.setTextColor(Color.parseColor("#7D88A6"));
            emptyView.setTextSize(14);
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(dp(8), dp(24), dp(8), dp(24));
            categoryGridContainer.addView(emptyView);
            return;
        }

        for (int i = 0; i < categories.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setBaselineAligned(false);

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            if (i > 0) {
                rowParams.setMargins(0, dp(14), 0, 0);
            }
            row.setLayoutParams(rowParams);

            row.addView(createCategoryCard(categories.get(i), true));
            if (i + 1 < categories.size()) {
                row.addView(createCategoryCard(categories.get(i + 1), false));
            } else {
                View spacer = new View(this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(0, dp(120), 1f);
                spacerParams.setMargins(dp(7), 0, 0, 0);
                row.addView(spacer, spacerParams);
            }

            categoryGridContainer.addView(row);
        }
    }

    private MaterialCardView createCategoryCard(CategoryItem category, boolean leftColumn) {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(0, dp(120), 1f);
        cardParams.setMargins(leftColumn ? 0 : dp(7), 0, leftColumn ? dp(7) : 0, 0);
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(category.color);
        card.setCardElevation(dp(2));
        card.setRadius(dp(14));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> loadBooksByCategory(category.name));

        FrameLayout content = new FrameLayout(this);
        content.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        content.setPadding(dp(14), dp(12), dp(14), dp(12));

        TextView titleView = new TextView(this);
        titleView.setText(category.name);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(15);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setMaxLines(2);
        content.addView(titleView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.START
        ));

        TextView countView = new TextView(this);
        countView.setText(category.count + " nội dung");
        countView.setTextColor(Color.parseColor("#E9EEFF"));
        countView.setTextSize(12);
        countView.setBackground(makeRoundedDrawable("#26FFFFFF", dp(12)));
        countView.setPadding(dp(8), dp(4), dp(8), dp(4));
        content.addView(countView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM | Gravity.START
        ));

        ImageView iconView = new ImageView(this);
        iconView.setImageResource(category.iconResId);
        iconView.setColorFilter(Color.parseColor("#CCFFFFFF"));
        iconView.setAlpha(0.72f);
        content.addView(iconView, new FrameLayout.LayoutParams(
                dp(38),
                dp(38),
                Gravity.BOTTOM | Gravity.END
        ));

        card.addView(content);
        return card;
    }

    private void setupSearchInputListener() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammaticChange) return;

                String query = s.toString().trim();
                if (query.isEmpty()) {
                    if (resultsContainer != null && defaultSearchContent != null) {
                        resultsContainer.setVisibility(View.GONE);
                        defaultSearchContent.setVisibility(View.VISIBLE);
                    }
                } else {
                    searchBooks(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadBooksByCategory(String category) {
        if (defaultSearchContent != null) defaultSearchContent.setVisibility(View.GONE);
        if (resultsContainer != null) resultsContainer.setVisibility(View.VISIBLE);
        if (tvResultsTitle != null) tvResultsTitle.setText("Thể loại: " + category);

        isProgrammaticChange = true;
        if (etSearch != null) etSearch.setText("");
        isProgrammaticChange = false;

        new Thread(() -> {
            List<Book> allBooks = dao.getAllBooks();
            List<Book> filteredBooks = new ArrayList<>();
            for (Book book : allBooks) {
                if (category.equals(normalizeCategory(book.category))) {
                    filteredBooks.add(book);
                }
            }

            runOnUiThread(() -> renderBookList(filteredBooks));
        }).start();
    }

    private void searchBooks(String query) {
        if (defaultSearchContent != null) defaultSearchContent.setVisibility(View.GONE);
        if (resultsContainer != null) resultsContainer.setVisibility(View.VISIBLE);
        if (tvResultsTitle != null) tvResultsTitle.setText("Kết quả cho: \"" + query + "\"");

        new Thread(() -> {
            List<Book> results = dao.searchBooks(query);
            runOnUiThread(() -> renderBookList(results));
        }).start();
    }

    private void renderBookList(List<Book> books) {
        if (searchBookListContainer != null) {
            BookListRenderer.render(
                    this,
                    searchBookListContainer,
                    books,
                    repository::setFavoriteForCurrentUser
            );
        }
    }

    private void showDefaultCategories() {
        isProgrammaticChange = true;
        if (etSearch != null) etSearch.setText("");
        isProgrammaticChange = false;

        if (resultsContainer != null) resultsContainer.setVisibility(View.GONE);
        if (defaultSearchContent != null) defaultSearchContent.setVisibility(View.VISIBLE);
    }

    private int getCategoryColor(int index) {
        return Color.parseColor(CATEGORY_COLORS[index % CATEGORY_COLORS.length]);
    }

    private int getCategoryIcon(int index) {
        return CATEGORY_ICONS[index % CATEGORY_ICONS.length];
    }

    private String normalizeCategory(String category) {
        return category == null ? "" : category.trim();
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

    private static class CategoryItem {
        final String name;
        final int count;
        final int color;
        final int iconResId;

        CategoryItem(String name, int count, int color, int iconResId) {
            this.name = name;
            this.count = count;
            this.color = color;
            this.iconResId = iconResId;
        }
    }
}
