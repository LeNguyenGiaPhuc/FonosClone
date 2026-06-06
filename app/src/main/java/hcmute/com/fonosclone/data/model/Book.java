package hcmute.com.fonosclone.data.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String author;
    public String type; // AUDIOBOOK, EBOOK, SUMMARY
    public String coverImage;
    public String audioResName;
    public String audioUrl;
    public String audioStoragePath;
    public boolean isFavorite;
    public String category;

    @Ignore
    public Book(String title, String author, String type, String coverImage, boolean isFavorite) {
        this(title, author, type, coverImage, "demo_audio", isFavorite, "K\u1ef9 n\u0103ng s\u1ed1ng", "", "");
    }

    @Ignore
    public Book(String title, String author, String type, String coverImage, String audioResName, boolean isFavorite) {
        this(title, author, type, coverImage, audioResName, isFavorite, "K\u1ef9 n\u0103ng s\u1ed1ng", "", "");
    }

    public Book(String title, String author, String type, String coverImage, String audioResName, boolean isFavorite, String category) {
        this(title, author, type, coverImage, audioResName, isFavorite, category, "", "");
    }

    @Ignore
    public Book(
            String title,
            String author,
            String type,
            String coverImage,
            String audioResName,
            boolean isFavorite,
            String category,
            String audioUrl,
            String audioStoragePath
    ) {
        this.title = title;
        this.author = author;
        this.type = type;
        this.coverImage = coverImage;
        this.audioResName = isBlank(audioResName) ? "demo_audio" : audioResName;
        this.audioUrl = audioUrl == null ? "" : audioUrl;
        this.audioStoragePath = audioStoragePath == null ? "" : audioStoragePath;
        this.isFavorite = isFavorite;
        this.category = category;
    }

    public String getPlayableAudioSource() {
        if (!isBlank(audioUrl)) {
            return audioUrl;
        }
        if (!isBlank(audioResName)) {
            return audioResName;
        }
        return "demo_audio";
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
