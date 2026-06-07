package hcmute.com.fonosclone.data.model;

import androidx.room.Entity;
import androidx.annotation.NonNull;
import androidx.room.Ignore;

@Entity(tableName = "challenge_completions", primaryKeys = {"userId", "missionId", "periodKey"})
public class ChallengeCompletion {
    @NonNull
    public String userId;
    @NonNull
    public String missionId;
    @NonNull
    public String periodKey;
    public int pointsAwarded;
    public long completedAt;

    @Ignore
    public ChallengeCompletion(@NonNull String missionId, @NonNull String periodKey, int pointsAwarded, long completedAt) {
        this("guest", missionId, periodKey, pointsAwarded, completedAt);
    }

    public ChallengeCompletion(@NonNull String userId, @NonNull String missionId, @NonNull String periodKey, int pointsAwarded, long completedAt) {
        this.userId = userId;
        this.missionId = missionId;
        this.periodKey = periodKey;
        this.pointsAwarded = pointsAwarded;
        this.completedAt = completedAt;
    }
}
