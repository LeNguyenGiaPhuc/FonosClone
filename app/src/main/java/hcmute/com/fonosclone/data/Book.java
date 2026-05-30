package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "books")
public class Book {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String author;
    public String type; // AUDIOBOOK, EBOOK, SUMMARY
    public String coverImage;
    public boolean isFavorite;


    public Book(String title, String author, String type, String coverImage, boolean isFavorite) {
        this.title = title;
        this.author = author;
        this.type = type;
        this.coverImage = coverImage;
        this.isFavorite = isFavorite;
    }
}