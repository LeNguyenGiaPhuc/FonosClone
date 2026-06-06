package hcmute.com.fonosclone.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "listening_progress")
public class ListeningProgress {
    @PrimaryKey
    public int bookId;

    public int positionMs;
    public int durationMs;
    public long updatedAt;

    public ListeningProgress(int bookId, int positionMs, int durationMs, long updatedAt) {
        this.bookId = bookId;
        this.positionMs = positionMs;
        this.durationMs = durationMs;
        this.updatedAt = updatedAt;
    }
}
