package hcmute.com.fonosclone;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;

public class SearchActivity extends BaseActivity {

    private EditText etSearch;
    private LinearLayout defaultSearchContent;
    private LinearLayout resultsContainer;
    private LinearLayout searchBookListContainer;
    private TextView tvResultsTitle;
    private FonosDao dao;
    
    // Cờ chống vòng lặp trigger ngược từ TextWatcher khi thay đổi text bằng code
    private boolean isProgrammaticChange = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_EXPLORE);
        setupUserMenu();

        dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();

        // Ánh xạ views
        etSearch = findViewById(R.id.etSearch);
        defaultSearchContent = findViewById(R.id.defaultSearchContent);
        resultsContainer = findViewById(R.id.resultsContainer);
        searchBookListContainer = findViewById(R.id.searchBookListContainer);
        tvResultsTitle = findViewById(R.id.tvResultsTitle);

        setupCategoryClickListeners();
        setupSearchInputListener();

        // Nút "Quay lại" danh sách Thể loại
        findViewById(R.id.btnBackToCategories).setOnClickListener(v -> showDefaultCategories());
    }

    private void setupCategoryClickListeners() {
        MaterialCardView cardVanHoc = findViewById(R.id.cardVanHoc);
        MaterialCardView cardKyNang = findViewById(R.id.cardKyNang);
        MaterialCardView cardKinhDoanh = findViewById(R.id.cardKinhDoanh);
        MaterialCardView cardThieuNhi = findViewById(R.id.cardThieuNhi);
        MaterialCardView cardTamLy = findViewById(R.id.cardTamLy);
        MaterialCardView cardTamLinh = findViewById(R.id.cardTamLinh);

        if (cardVanHoc != null) {
            cardVanHoc.setOnClickListener(v -> loadBooksByCategory("Văn học & Truyện"));
        }
        if (cardKyNang != null) {
            cardKyNang.setOnClickListener(v -> loadBooksByCategory("Kỹ năng sống"));
        }
        if (cardKinhDoanh != null) {
            cardKinhDoanh.setOnClickListener(v -> loadBooksByCategory("Kinh doanh"));
        }
        if (cardThieuNhi != null) {
            cardThieuNhi.setOnClickListener(v -> loadBooksByCategory("Thiếu nhi"));
        }
        if (cardTamLy != null) {
            cardTamLy.setOnClickListener(v -> loadBooksByCategory("Tâm lý học"));
        }
        if (cardTamLinh != null) {
            cardTamLinh.setOnClickListener(v -> loadBooksByCategory("Tâm linh & Tôn giáo"));
        }
    }

    private void setupSearchInputListener() {
        if (etSearch == null) return;
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isProgrammaticChange) return; // Bỏ qua nếu text thay đổi bằng code
                
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    // Nếu xoá hết chữ -> Hiển thị lại Grid Thể loại mặc định
                    if (resultsContainer != null && defaultSearchContent != null) {
                        resultsContainer.setVisibility(View.GONE);
                        defaultSearchContent.setVisibility(View.VISIBLE);
                    }
                } else {
                    // Tìm kiếm động theo từ khóa người dùng gõ
                    searchBooks(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Tải sách theo từng Thể loại cụ thể (Category)
     */
    private void loadBooksByCategory(String category) {
        if (defaultSearchContent != null) defaultSearchContent.setVisibility(View.GONE);
        if (resultsContainer != null) resultsContainer.setVisibility(View.VISIBLE);
        if (tvResultsTitle != null) tvResultsTitle.setText("Thể loại: " + category);
        
        // Đặt cờ trước khi set rỗng ô search để tránh TextWatcher reset lại giao diện
        isProgrammaticChange = true;
        if (etSearch != null) etSearch.setText("");
        isProgrammaticChange = false;

        new Thread(() -> {
            List<Book> allBooks = dao.getAllBooks();
            List<Book> filteredBooks = new ArrayList<>();
            for (Book book : allBooks) {
                if (category.equals(book.category)) {
                    filteredBooks.add(book);
                }
            }

            runOnUiThread(() -> renderBookList(filteredBooks));
        }).start();
    }

    /**
     * Tìm kiếm sách theo từ khóa nhập vào
     */
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
                    (book, newFavoriteValue) -> new Thread(() -> dao.setFavorite(book.id, newFavoriteValue)).start()
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
}
