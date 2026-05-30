package hcmute.com.fonosclone.data;

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
    public boolean isFavorite;

    @Ignore
    public Book(String title, String author, String type, String coverImage, boolean isFavorite) {
        this.title = title;
        this.author = author;
        this.type = type;
        this.coverImage = coverImage;
        this.audioResName = "demo_audio";
        this.isFavorite = isFavorite;
    }

    public Book(String title, String author, String type, String coverImage, String audioResName, boolean isFavorite) {
        this.title = title;
        this.author = author;
        this.type = type;
        this.coverImage = coverImage;
        this.audioResName = audioResName;
        this.isFavorite = isFavorite;
    }
}
