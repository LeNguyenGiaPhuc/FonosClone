package hcmute.com.fonosclone.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "downloaded_content", primaryKeys = {"userId", "bookId"})
public class DownloadedContent {
    @NonNull
    public String userId;
    public int bookId;

    public long downloadedAt;
    public String localAudioPath;
    public String remoteAudioSource;

    @Ignore
    public DownloadedContent(int bookId, long downloadedAt) {
        this("guest", bookId, downloadedAt, "", "");
    }

    @Ignore
    public DownloadedContent(int bookId, long downloadedAt, String localAudioPath, String remoteAudioSource) {
        this("guest", bookId, downloadedAt, localAudioPath, remoteAudioSource);
    }

    public DownloadedContent(@NonNull String userId, int bookId, long downloadedAt, String localAudioPath, String remoteAudioSource) {
        this.userId = userId;
        this.bookId = bookId;
        this.downloadedAt = downloadedAt;
        this.localAudioPath = localAudioPath == null ? "" : localAudioPath;
        this.remoteAudioSource = remoteAudioSource == null ? "" : remoteAudioSource;
    }
}
