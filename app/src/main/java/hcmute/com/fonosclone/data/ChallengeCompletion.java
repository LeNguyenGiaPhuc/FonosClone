package hcmute.com.fonosclone.data;

import androidx.room.Entity;
import androidx.annotation.NonNull;

@Entity(tableName = "challenge_completions", primaryKeys = {"missionId", "periodKey"})
public class ChallengeCompletion {
    @NonNull
    public String missionId;
    @NonNull
    public String periodKey;
    public int pointsAwarded;
    public long completedAt;

    public ChallengeCompletion(@NonNull String missionId, @NonNull String periodKey, int pointsAwarded, long completedAt) {
        this.missionId = missionId;
        this.periodKey = periodKey;
        this.pointsAwarded = pointsAwarded;
        this.completedAt = completedAt;
    }
}
