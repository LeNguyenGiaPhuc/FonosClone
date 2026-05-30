package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pod_courses")
public class PodCourse {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String teacher;
    public String category;
    public String coverColor;
    public double rating;

    public PodCourse(String title, String teacher, String category, String coverColor, double rating) {
        this.title = title;
        this.teacher = teacher;
        this.category = category;
        this.coverColor = coverColor;
        this.rating = rating;
    }
}