package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "listening_history")
public class ListeningHistory {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int bookId;
    public int listenedSeconds;

    public ListeningHistory(int bookId, int listenedSeconds) {
        this.bookId = bookId;
        this.listenedSeconds = listenedSeconds;
    }
}
