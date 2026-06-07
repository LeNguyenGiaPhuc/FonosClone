package hcmute.com.fonosclone.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "favorite_content", primaryKeys = {"userId", "bookId"})
public class FavoriteContent {
    @NonNull
    public String userId;
    public int bookId;
    public long updatedAt;

    public FavoriteContent(@NonNull String userId, int bookId, long updatedAt) {
        this.userId = userId;
        this.bookId = bookId;
        this.updatedAt = updatedAt;
    }
}
