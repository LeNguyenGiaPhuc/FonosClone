package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "listening_history")
public class ListeningHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int bookId;
    public int listenedSeconds;
    public long listenedAt;

    @Ignore
    public ListeningHistory(int bookId, int listenedSeconds) {
        this(bookId, listenedSeconds, System.currentTimeMillis());
    }

    public ListeningHistory(int bookId, int listenedSeconds, long listenedAt) {
        this.bookId = bookId;
        this.listenedSeconds = listenedSeconds;
        this.listenedAt = listenedAt;
    }
}
