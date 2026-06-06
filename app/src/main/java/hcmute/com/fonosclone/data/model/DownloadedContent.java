package hcmute.com.fonosclone.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "downloaded_content")
public class DownloadedContent {
    @PrimaryKey
    public int bookId;

    public long downloadedAt;
    public String localAudioPath;
    public String remoteAudioSource;

    public DownloadedContent(int bookId, long downloadedAt) {
        this(bookId, downloadedAt, "", "");
    }

    @Ignore
    public DownloadedContent(int bookId, long downloadedAt, String localAudioPath, String remoteAudioSource) {
        this.bookId = bookId;
        this.downloadedAt = downloadedAt;
        this.localAudioPath = localAudioPath == null ? "" : localAudioPath;
        this.remoteAudioSource = remoteAudioSource == null ? "" : remoteAudioSource;
    }
}
