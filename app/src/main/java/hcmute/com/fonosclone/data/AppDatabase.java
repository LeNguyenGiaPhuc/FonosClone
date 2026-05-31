package hcmute.com.fonosclone.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Book.class, PodCourse.class, ListeningHistory.class},
        version = 6,
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
