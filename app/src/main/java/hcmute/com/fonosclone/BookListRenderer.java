package hcmute.com.fonosclone;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import hcmute.com.fonosclone.data.Book;

public final class BookListRenderer {
    public interface FavoriteClickListener {
        void onFavoriteClicked(Book book, boolean newFavoriteValue);
    }

    private BookListRenderer() {
    }

    public static void render(
            Activity activity,
            LinearLayout container,
            List<Book> books,
            FavoriteClickListener favoriteClickListener
    ) {
        container.removeAllViews();

        if (books == null || books.isEmpty()) {
            TextView emptyView = new TextView(activity);
            emptyView.setText(R.string.no_books_yet);
            emptyView.setTextColor(Color.parseColor("#7D88A6"));
            emptyView.setTextSize(14);
            emptyView.setPadding(dp(activity, 8), dp(activity, 20), dp(activity, 8), dp(activity, 20));
            container.addView(emptyView);
            return;
        }

        for (Book book : books) {
            container.addView(createBookRow(activity, book, favoriteClickListener));
        }
    }

    private static MaterialCardView createBookRow(
            Activity activity,
            Book book,
            FavoriteClickListener favoriteClickListener
    ) {
        MaterialCardView card = new MaterialCardView(activity);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(activity, 10));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(dp(activity, 2));
        card.setRadius(dp(activity, 8));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> BookNavigator.openPlayer(activity, book));

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12));

        ImageView coverView = new ImageView(activity);
        LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(dp(activity, 72), dp(activity, 104));
        coverView.setLayoutParams(coverParams);
        coverView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        coverView.setContentDescription(activity.getString(R.string.book_cover));
        int coverResId = activity.getResources().getIdentifier(book.coverImage, "drawable", activity.getPackageName());
        if (coverResId != 0) {
            coverView.setImageResource(coverResId);
        }
        row.addView(coverView);

        LinearLayout textColumn = new LinearLayout(activity);
        textColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        textParams.setMargins(dp(activity, 12), 0, dp(activity, 8), 0);
        textColumn.setLayoutParams(textParams);

        TextView titleView = new TextView(activity);
        titleView.setText(book.title);
        titleView.setTextColor(Color.parseColor("#1A2B5F"));
        titleView.setTextSize(14);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        titleView.setMaxLines(2);
        textColumn.addView(titleView);

        TextView authorView = new TextView(activity);
        authorView.setText(book.author);
        authorView.setTextColor(Color.parseColor("#8892AE"));
        authorView.setTextSize(12);
        authorView.setMaxLines(1);
        textColumn.addView(authorView);

        TextView typeView = new TextView(activity);
        typeView.setText(book.type);
        typeView.setTextColor(Color.parseColor("#F07830"));
        typeView.setTextSize(11);
        typeView.setPadding(0, dp(activity, 6), 0, 0);
        textColumn.addView(typeView);
        row.addView(textColumn);

        LinearLayout actionColumn = new LinearLayout(activity);
        actionColumn.setOrientation(LinearLayout.VERTICAL);

        Button playButton = new Button(activity);
        playButton.setText(R.string.play);
        playButton.setTextSize(12);
        playButton.setAllCaps(false);
        playButton.setOnClickListener(v -> BookNavigator.openPlayer(activity, book));
        actionColumn.addView(playButton, new LinearLayout.LayoutParams(dp(activity, 88), dp(activity, 42)));

        if (favoriteClickListener != null) {
            Button favoriteButton = new Button(activity);
            favoriteButton.setText(book.isFavorite ? R.string.saved : R.string.tab_favorite);
            favoriteButton.setTextSize(11);
            favoriteButton.setAllCaps(false);
            favoriteButton.setOnClickListener(v -> {
                boolean newFavoriteValue = !book.isFavorite;
                book.isFavorite = newFavoriteValue;
                favoriteButton.setText(newFavoriteValue ? R.string.saved : R.string.tab_favorite);
                favoriteClickListener.onFavoriteClicked(book, newFavoriteValue);
            });
            actionColumn.addView(favoriteButton, new LinearLayout.LayoutParams(dp(activity, 88), dp(activity, 42)));
        }

        row.addView(actionColumn);
        card.addView(row);
        return card;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }
}
