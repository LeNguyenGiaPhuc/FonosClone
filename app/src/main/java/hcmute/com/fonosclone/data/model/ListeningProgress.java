package hcmute.com.fonosclone.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "listening_progress", primaryKeys = {"userId", "bookId"})
public class ListeningProgress {
    @NonNull
    public String userId;
    public int bookId;

    public int positionMs;
    public int durationMs;
    public long updatedAt;

    @Ignore
    public ListeningProgress(int bookId, int positionMs, int durationMs, long updatedAt) {
        this("guest", bookId, positionMs, durationMs, updatedAt);
    }

    public ListeningProgress(@NonNull String userId, int bookId, int positionMs, int durationMs, long updatedAt) {
        this.userId = userId;
        this.bookId = bookId;
        this.positionMs = positionMs;
        this.durationMs = durationMs;
        this.updatedAt = updatedAt;
    }
}
