package hcmute.com.fonosclone.ui.navigation;


import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.service.AudioPlayerService;
import hcmute.com.fonosclone.ui.activity.PlayerActivity;

import android.content.Context;
import android.content.Intent;


public final class BookNavigator {
    private BookNavigator() {
    }

    public static void openPlayer(Context context, Book book) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(AudioPlayerService.EXTRA_TITLE, book.title);
        intent.putExtra(AudioPlayerService.EXTRA_AUTHOR, book.author);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_RES, book.audioResName);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_URL, book.audioUrl);
        intent.putExtra(AudioPlayerService.EXTRA_AUDIO_STORAGE_PATH, book.audioStoragePath);
        intent.putExtra(PlayerActivity.EXTRA_BOOK_ID, book.id);
        intent.putExtra("cover_image", book.coverImage);
        context.startActivity(intent);
    }
}
