package hcmute.com.fonosclone.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_points")
public class UserPoints {
    @PrimaryKey
    public int id;

    public int totalPoints;

    public UserPoints(int id, int totalPoints) {
        this.id = id;
        this.totalPoints = totalPoints;
    }
}
