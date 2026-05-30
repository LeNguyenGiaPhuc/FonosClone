package hcmute.com.fonosclone;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hcmute.com.fonosclone.data.AppDatabase;
import hcmute.com.fonosclone.data.Book;
import hcmute.com.fonosclone.data.FonosDao;
import hcmute.com.fonosclone.data.SeedData;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        applySystemBarPadding(R.id.main);
        setupBottomNavigation(NAV_BOOKS);
        setupTabs(TAB_AUDIOBOOKS);
        setupUserMenu();

        AppDatabase db = AppDatabase.getInstance(this);

        new Thread(() -> {
            FonosDao dao = db.fonosDao();

            SeedData.insertSampleData(dao);

            List<Book> audiobooks = dao.getBooksByType("AUDIOBOOK");

            runOnUiThread(() -> {
                if (audiobooks.size() > 0) {
                    bindCover(audiobooks.get(0), R.id.ivCover1);
                    bindBook(audiobooks.get(0), R.id.ivGridCover1, R.id.tvTitle1, R.id.tvAuthor1);
                }

                if (audiobooks.size() > 1) {
                    bindCover(audiobooks.get(1), R.id.ivCover2);
                    bindBook(audiobooks.get(1), R.id.ivGridCover2, R.id.tvTitle2, R.id.tvAuthor2);
                }

                if (audiobooks.size() > 2) {
                    bindCover(audiobooks.get(2), R.id.ivCover3);
                    bindBook(audiobooks.get(2), R.id.ivGridCover3, R.id.tvTitle3, R.id.tvAuthor3);
                }
            });
        }).start();
    }

    private void bindBook(Book book, int coverViewId, int titleViewId, int authorViewId) {
        bindCover(book, coverViewId);

        TextView titleView = findViewById(titleViewId);
        TextView authorView = findViewById(authorViewId);

        titleView.setText(book.title);
        authorView.setText(book.author);
    }

    private void bindCover(Book book, int coverViewId) {
        ImageView coverView = findViewById(coverViewId);
        int imageResId = getResources().getIdentifier(
                book.coverImage,
                "drawable",
                getPackageName()
        );

        if (imageResId != 0) {
            coverView.setImageResource(imageResId);
        }
    }
}
