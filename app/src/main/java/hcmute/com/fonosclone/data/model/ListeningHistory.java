package hcmute.com.fonosclone.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "listening_history")
public class ListeningHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String userId;
    public int bookId;
    public int listenedSeconds;
    public long listenedAt;

    @Ignore
    public ListeningHistory(int bookId, int listenedSeconds) {
        this("guest", bookId, listenedSeconds, System.currentTimeMillis());
    }

    @Ignore
    public ListeningHistory(int bookId, int listenedSeconds, long listenedAt) {
        this("guest", bookId, listenedSeconds, listenedAt);
    }

    public ListeningHistory(@NonNull String userId, int bookId, int listenedSeconds, long listenedAt) {
        this.userId = userId;
        this.bookId = bookId;
        this.listenedSeconds = listenedSeconds;
        this.listenedAt = listenedAt;
    }
}
