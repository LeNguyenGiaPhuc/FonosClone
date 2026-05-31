package hcmute.com.fonosclone;

import android.os.Bundle;
import android.widget.LinearLayout;

import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.SeedData;

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
        new Thread(() -> {
            FonosDao dao = AppDatabase.getInstance(getApplicationContext()).fonosDao();
            SeedData.insertSampleData(dao);
            List<Book> books = dao.getBooksByType("EBOOK");

            runOnUiThread(() -> BookListRenderer.render(
                    this,
                    container,
                    books,
                    (book, newFavoriteValue) -> new Thread(() ->
                            AppDatabase.getInstance(getApplicationContext())
                                    .fonosDao()
                                    .setFavorite(book.id, newFavoriteValue)
                    ).start()
            ));
        }).start();
    }
}
