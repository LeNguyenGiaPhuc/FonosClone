package hcmute.com.fonosclone;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;

public class LibraryActivity extends BaseActivity {
    private static final int TAB_DOWNLOADED = 0;
    private static final int TAB_PURCHASED = 1;
    private static final int TAB_FAVORITE = 2;

    private int activeTab = TAB_DOWNLOADED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_LIBRARY);
        setupUserMenu();

        setupTabs();
        loadTabData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load lại dữ liệu mỗi khi quay lại màn hình thư viện (để cập nhật trạng thái Yêu thích mới nhất)
        loadTabData();
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

        // Cập nhật giao diện tabs
        updateTabUI(R.id.tvTabDownloaded, R.id.indicatorDownloaded, activeTab == TAB_DOWNLOADED);
        updateTabUI(R.id.tvTabPurchased, R.id.indicatorPurchased, activeTab == TAB_PURCHASED);
        updateTabUI(R.id.tvTabFavorite, R.id.indicatorFavorite, activeTab == TAB_FAVORITE);

        // Nạp lại dữ liệu tương ứng
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

    private void loadTabData() {
        LinearLayout bookListContainer = findViewById(R.id.bookListContainer);
        LinearLayout emptyStateContainer = findViewById(R.id.emptyStateContainer);
        View scrollViewContainer = findViewById(R.id.scrollViewContainer);

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            FonosDao dao = db.fonosDao();
            List<Book> books = new ArrayList<>();

            // Truy vấn dữ liệu theo tab tương ứng
            if (activeTab == TAB_DOWNLOADED) {
                // Lấy sách nói (AUDIOBOOK) làm danh sách sách tải xuống demo
                books = dao.getBooksByType("AUDIOBOOK");
            } else if (activeTab == TAB_PURCHASED) {
                // Lấy sách điện tử (EBOOK) làm danh sách sách đã mua demo
                books = dao.getBooksByType("EBOOK");
            } else if (activeTab == TAB_FAVORITE) {
                // Lấy sách được người dùng đánh dấu là yêu thích thực tế từ database
                books = dao.getFavoriteBooks();
            }

            final List<Book> finalBooks = books;

            runOnUiThread(() -> {
                if (finalBooks == null || finalBooks.isEmpty()) {
                    if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                    if (scrollViewContainer != null) scrollViewContainer.setVisibility(View.GONE);
                } else {
                    if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                    if (scrollViewContainer != null) scrollViewContainer.setVisibility(View.VISIBLE);

                    if (bookListContainer != null) {
                        BookListRenderer.render(
                                this,
                                bookListContainer,
                                finalBooks,
                                (book, newFavoriteValue) -> new Thread(() -> {
                                    dao.setFavorite(book.id, newFavoriteValue);
                                    // Sau khi bỏ/thêm yêu thích ở màn hình Thư viện, nạp lại tab ngay lập tức
                                    runOnUiThread(this::loadTabData);
                                }).start()
                        );
                    }
                }
            });
        }).start();
    }
}
