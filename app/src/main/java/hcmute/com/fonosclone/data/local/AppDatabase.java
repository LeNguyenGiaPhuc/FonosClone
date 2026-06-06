package hcmute.com.fonosclone.data.local;


import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.ChallengeCompletion;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.data.model.PodCourse;
import hcmute.com.fonosclone.data.model.UserPoints;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Book.class, PodCourse.class, ListeningHistory.class, ListeningProgress.class, DownloadedContent.class, ChallengeCompletion.class, UserPoints.class},
        version = 10,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract FonosDao fonosDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "fonos_database"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
