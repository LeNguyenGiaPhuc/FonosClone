package hcmute.com.fonosclone.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_points")
public class UserPoints {
    @PrimaryKey
    @NonNull
    public String userId;

    public int totalPoints;

    @Ignore
    public UserPoints(int id, int totalPoints) {
        this("guest", totalPoints);
    }

    public UserPoints(@NonNull String userId, int totalPoints) {
        this.userId = userId;
        this.totalPoints = totalPoints;
    }
}
