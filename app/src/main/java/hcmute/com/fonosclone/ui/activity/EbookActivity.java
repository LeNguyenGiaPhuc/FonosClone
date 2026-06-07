package hcmute.com.fonosclone.ui.activity;


import hcmute.com.fonosclone.auth.UserIdentity;
import hcmute.com.fonosclone.data.local.AppDatabase;
import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.repository.FonosRepository;
import hcmute.com.fonosclone.data.seed.SeedData;
import hcmute.com.fonosclone.R;
import hcmute.com.fonosclone.ui.renderer.BookListRenderer;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;


public class EbookActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ebooks);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_BOOKS);
        setupTabs(TAB_EBOOKS);
        setupUserMenu();

        loadBooks();
    }

    private void loadBooks() {
        LinearLayout container = findViewById(R.id.bookListContainer);
        TextView titleView = findViewById(R.id.tvListTitle);
        FonosRepository repository = new FonosRepository(this);
        new Thread(() -> {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            SeedData.insertSampleData(dao);
            List<Book> books = dao.getBooksByTypeForUser("EBOOK", UserIdentity.getCurrentUserId(getApplicationContext()));

            runOnUiThread(() -> {
                if (titleView != null) {
                    titleView.setText(getString(R.string.ebooks_count_title, books.size()));
                }
                BookListRenderer.render(
                        this,
                        container,
                        books,
                        repository::setFavoriteForCurrentUser
                );
            });
        }).start();
    }
}
