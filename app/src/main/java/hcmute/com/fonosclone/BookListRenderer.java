package hcmute.com.fonosclone;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
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
            emptyView.setGravity(Gravity.CENTER);
            emptyView.setPadding(dp(activity, 8), dp(activity, 28), dp(activity, 8), dp(activity, 28));
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
        cardParams.setMargins(0, 0, 0, dp(activity, 12));
        card.setLayoutParams(cardParams);
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(dp(activity, 3));
        card.setRadius(dp(activity, 18));
        card.setStrokeWidth(dp(activity, 1));
        card.setStrokeColor(Color.parseColor("#E5E9F5"));
        card.setClickable(true);
        card.setFocusable(true);
        card.setOnClickListener(v -> BookNavigator.openPlayer(activity, book));

        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12));

        ImageView coverView = new ImageView(activity);
        LinearLayout.LayoutParams coverParams = new LinearLayout.LayoutParams(dp(activity, 78), dp(activity, 112));
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
        textColumn.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        textParams.setMargins(dp(activity, 12), 0, dp(activity, 10), 0);
        textColumn.setLayoutParams(textParams);

        TextView titleView = new TextView(activity);
        titleView.setText(book.title);
        titleView.setTextColor(Color.parseColor("#1A2B5F"));
        titleView.setTextSize(14);
        titleView.setTypeface(null, Typeface.BOLD);
        titleView.setMaxLines(2);
        textColumn.addView(titleView);

        TextView authorView = new TextView(activity);
        authorView.setText(book.author);
        authorView.setTextColor(Color.parseColor("#8892AE"));
        authorView.setTextSize(12);
        authorView.setMaxLines(1);
        authorView.setPadding(0, dp(activity, 4), 0, 0);
        textColumn.addView(authorView);

        TextView typeView = new TextView(activity);
        String subText = (book.category != null ? book.category : "Lifestyle") + " | " + book.type;
        typeView.setText(subText);
        typeView.setTextColor(Color.parseColor("#6F4DB2"));
        typeView.setTextSize(11);
        typeView.setTypeface(null, Typeface.BOLD);
        typeView.setPadding(dp(activity, 9), dp(activity, 5), dp(activity, 9), dp(activity, 5));
        typeView.setBackground(makeRoundedDrawable("#F0ECFA", dp(activity, 13)));
        LinearLayout.LayoutParams typeParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        typeParams.setMargins(0, dp(activity, 10), 0, 0);
        textColumn.addView(typeView, typeParams);
        row.addView(textColumn);

        LinearLayout actionColumn = new LinearLayout(activity);
        actionColumn.setOrientation(LinearLayout.VERTICAL);
        actionColumn.setGravity(Gravity.CENTER);

        Button playButton = new Button(activity);
        playButton.setText(R.string.play);
        playButton.setTextSize(12);
        playButton.setTextColor(Color.WHITE);
        playButton.setTypeface(null, Typeface.BOLD);
        playButton.setAllCaps(false);
        playButton.setMinWidth(0);
        playButton.setMinHeight(0);
        playButton.setPadding(0, 0, 0, 0);
        playButton.setBackground(makeRoundedDrawable("#F07830", dp(activity, 17)));
        playButton.setOnClickListener(v -> BookNavigator.openPlayer(activity, book));
        actionColumn.addView(playButton, new LinearLayout.LayoutParams(dp(activity, 82), dp(activity, 36)));

        if (favoriteClickListener != null) {
            Button favoriteButton = new Button(activity);
            favoriteButton.setText(book.isFavorite ? R.string.saved : R.string.tab_favorite);
            favoriteButton.setTextSize(11);
            favoriteButton.setTextColor(book.isFavorite ? Color.WHITE : Color.parseColor("#6F4DB2"));
            favoriteButton.setTypeface(null, Typeface.BOLD);
            favoriteButton.setAllCaps(false);
            favoriteButton.setMinWidth(0);
            favoriteButton.setMinHeight(0);
            favoriteButton.setPadding(0, 0, 0, 0);
            favoriteButton.setBackground(makeFavoriteBackground(activity, book.isFavorite));
            favoriteButton.setOnClickListener(v -> {
                boolean newFavoriteValue = !book.isFavorite;
                book.isFavorite = newFavoriteValue;
                favoriteButton.setText(newFavoriteValue ? R.string.saved : R.string.tab_favorite);
                favoriteButton.setTextColor(newFavoriteValue ? Color.WHITE : Color.parseColor("#6F4DB2"));
                favoriteButton.setBackground(makeFavoriteBackground(activity, newFavoriteValue));
                favoriteClickListener.onFavoriteClicked(book, newFavoriteValue);
            });
            LinearLayout.LayoutParams favoriteParams = new LinearLayout.LayoutParams(dp(activity, 82), dp(activity, 36));
            favoriteParams.setMargins(0, dp(activity, 8), 0, 0);
            actionColumn.addView(favoriteButton, favoriteParams);
        }

        row.addView(actionColumn);
        card.addView(row);
        return card;
    }

    private static int dp(Activity activity, int value) {
        return Math.round(value * activity.getResources().getDisplayMetrics().density);
    }

    private static GradientDrawable makeRoundedDrawable(String color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.parseColor(color));
        drawable.setCornerRadius(radius);
        return drawable;
    }

    private static GradientDrawable makeFavoriteBackground(Activity activity, boolean selected) {
        GradientDrawable drawable = makeRoundedDrawable(selected ? "#6F4DB2" : "#FFFFFF", dp(activity, 17));
        drawable.setStroke(dp(activity, 1), Color.parseColor("#6F4DB2"));
        return drawable;
    }
}
