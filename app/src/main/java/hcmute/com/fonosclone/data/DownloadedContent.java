package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloaded_content")
public class DownloadedContent {
    @PrimaryKey
    public int bookId;

    public long downloadedAt;

    public DownloadedContent(int bookId, long downloadedAt) {
        this.bookId = bookId;
        this.downloadedAt = downloadedAt;
    }
}
