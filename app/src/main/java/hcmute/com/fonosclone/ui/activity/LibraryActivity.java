package hcmute.com.fonosclone.ui.activity;


import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.repository.FonosRepository;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.ui.renderer.BookListRenderer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class LibraryActivity extends BaseActivity {
    private static final int TAB_DOWNLOADED = 0;
    private static final int TAB_PURCHASED = 1;
    private static final int TAB_FAVORITE = 2;

    private int activeTab = TAB_DOWNLOADED;
    private FonosRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_LIBRARY);
        setupUserMenu();

        repository = new FonosRepository(this);
        setupTabs();
        syncFavoritesAndLoad();
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncFavoritesAndLoad();
    }

    private void setupTabs() {
        LinearLayout tabDownloaded = findViewById(R.id.tabDownloaded);
        LinearLayout tabPurchased = findViewById(R.id.tabPurchased);
        LinearLayout tabFavorite = findViewById(R.id.tabFavorite);

        if (tabDownloaded != null) {
            tabDownloaded.setOnClickListener(v -> selectTab(TAB_DOWNLOADED));
        }
        if (tabPurchased != null) {
            tabPurchased.setOnClickListener(v -> selectTab(TAB_PURCHASED));
        }
        if (tabFavorite != null) {
            tabFavorite.setOnClickListener(v -> selectTab(TAB_FAVORITE));
        }
    }

    private void selectTab(int tabIndex) {
        if (activeTab == tabIndex) return;
        activeTab = tabIndex;

        updateTabUI(R.id.tvTabDownloaded, R.id.indicatorDownloaded, activeTab == TAB_DOWNLOADED);
        updateTabUI(R.id.tvTabPurchased, R.id.indicatorPurchased, activeTab == TAB_PURCHASED);
        updateTabUI(R.id.tvTabFavorite, R.id.indicatorFavorite, activeTab == TAB_FAVORITE);

        loadTabData();
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

    private void syncFavoritesAndLoad() {
        repository.syncFavoritesForCurrentUser(new FonosRepository.SyncCallback() {
            @Override
            public void onSuccess() {
                loadTabData();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                loadTabData();
            }
        });
    }

    private void loadTabData() {
        LinearLayout bookListContainer = findViewById(R.id.bookListContainer);
        LinearLayout emptyStateContainer = findViewById(R.id.emptyStateContainer);
        View scrollViewContainer = findViewById(R.id.scrollViewContainer);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            FonosDao dao = db.fonosDao();
            String userId = UserIdentity.getCurrentUserId(getApplicationContext());
            List<Book> books = new ArrayList<>();

            if (activeTab == TAB_DOWNLOADED) {
                books = dao.getDownloadedBooks(userId);
            } else if (activeTab == TAB_PURCHASED) {
                books = dao.getBooksByTypeForUser("EBOOK", userId);
            } else if (activeTab == TAB_FAVORITE) {
                books = dao.getFavoriteBooks(userId);
            }

            final List<Book> finalBooks = books;

            runOnUiThread(() -> {
                if (finalBooks == null || finalBooks.isEmpty()) {
                    if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                    if (scrollViewContainer != null) scrollViewContainer.setVisibility(View.GONE);
                    return;
                }

                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                if (scrollViewContainer != null) scrollViewContainer.setVisibility(View.VISIBLE);

                if (bookListContainer != null) {
                    BookListRenderer.render(
                            this,
                            bookListContainer,
                            finalBooks,
                            (book, newFavoriteValue) -> repository.setFavoriteForCurrentUser(
                                    book,
                                    newFavoriteValue,
                                    new FonosRepository.SyncCallback() {
                                        @Override
                                        public void onSuccess() {
                                            if (activeTab == TAB_FAVORITE) {
                                                loadTabData();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                            )
                    );
                }
            });
        }).start();
    }
}
