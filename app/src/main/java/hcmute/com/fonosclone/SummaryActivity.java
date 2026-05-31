package hcmute.com.fonosclone;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.SeedData;

public class SummaryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summaries);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_BOOKS);
        setupTabs(TAB_SUMMARIES);
        setupUserMenu();

        loadBooks();
    }

    private void loadBooks() {
        LinearLayout container = findViewById(R.id.bookListContainer);
        FonosRepository repository = new FonosRepository(this);
        new Thread(() -> {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            SeedData.insertSampleData(dao);
            List<Book> books = dao.getBooksByType("SUMMARY");

            runOnUiThread(() -> BookListRenderer.render(
                    this,
                    container,
                    books,
                    repository::setFavoriteForCurrentUser
            ));
        }).start();
    }
}
