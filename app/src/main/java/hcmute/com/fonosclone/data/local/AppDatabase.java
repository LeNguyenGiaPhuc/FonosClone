package hcmute.com.fonosclone.data.local;


import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.ChallengeCompletion;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.data.model.FavoriteContent;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.data.model.PodCourse;
import hcmute.com.fonosclone.data.model.UserPoints;

import android.content.Context;
import androidx.room.Database;
import androidx.room.migration.Migration;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {Book.class, PodCourse.class, FavoriteContent.class, ListeningHistory.class, ListeningProgress.class, DownloadedContent.class, ChallengeCompletion.class, UserPoints.class},
        version = 11,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    private static final Migration MIGRATION_10_11 = new Migration(10, 11) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `favorite_content` (`userId` TEXT NOT NULL, `bookId` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `bookId`))");
            database.execSQL("INSERT OR REPLACE INTO `favorite_content` (`userId`, `bookId`, `updatedAt`) SELECT 'guest', `id`, " + System.currentTimeMillis() + " FROM `books` WHERE `isFavorite` = 1");

            database.execSQL("CREATE TABLE IF NOT EXISTS `downloaded_content_new` (`userId` TEXT NOT NULL, `bookId` INTEGER NOT NULL, `downloadedAt` INTEGER NOT NULL, `localAudioPath` TEXT, `remoteAudioSource` TEXT, PRIMARY KEY(`userId`, `bookId`))");
            database.execSQL("INSERT OR REPLACE INTO `downloaded_content_new` (`userId`, `bookId`, `downloadedAt`, `localAudioPath`, `remoteAudioSource`) SELECT 'guest', `bookId`, `downloadedAt`, `localAudioPath`, `remoteAudioSource` FROM `downloaded_content`");
            database.execSQL("DROP TABLE `downloaded_content`");
            database.execSQL("ALTER TABLE `downloaded_content_new` RENAME TO `downloaded_content`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `listening_progress_new` (`userId` TEXT NOT NULL, `bookId` INTEGER NOT NULL, `positionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `bookId`))");
            database.execSQL("INSERT OR REPLACE INTO `listening_progress_new` (`userId`, `bookId`, `positionMs`, `durationMs`, `updatedAt`) SELECT 'guest', `bookId`, `positionMs`, `durationMs`, `updatedAt` FROM `listening_progress`");
            database.execSQL("DROP TABLE `listening_progress`");
            database.execSQL("ALTER TABLE `listening_progress_new` RENAME TO `listening_progress`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `listening_history_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` TEXT NOT NULL, `bookId` INTEGER NOT NULL, `listenedSeconds` INTEGER NOT NULL, `listenedAt` INTEGER NOT NULL)");
            database.execSQL("INSERT INTO `listening_history_new` (`id`, `userId`, `bookId`, `listenedSeconds`, `listenedAt`) SELECT `id`, 'guest', `bookId`, `listenedSeconds`, `listenedAt` FROM `listening_history`");
            database.execSQL("DROP TABLE `listening_history`");
            database.execSQL("ALTER TABLE `listening_history_new` RENAME TO `listening_history`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `challenge_completions_new` (`userId` TEXT NOT NULL, `missionId` TEXT NOT NULL, `periodKey` TEXT NOT NULL, `pointsAwarded` INTEGER NOT NULL, `completedAt` INTEGER NOT NULL, PRIMARY KEY(`userId`, `missionId`, `periodKey`))");
            database.execSQL("INSERT OR REPLACE INTO `challenge_completions_new` (`userId`, `missionId`, `periodKey`, `pointsAwarded`, `completedAt`) SELECT 'guest', `missionId`, `periodKey`, `pointsAwarded`, `completedAt` FROM `challenge_completions`");
            database.execSQL("DROP TABLE `challenge_completions`");
            database.execSQL("ALTER TABLE `challenge_completions_new` RENAME TO `challenge_completions`");

            database.execSQL("CREATE TABLE IF NOT EXISTS `user_points_new` (`userId` TEXT NOT NULL, `totalPoints` INTEGER NOT NULL, PRIMARY KEY(`userId`))");
            database.execSQL("INSERT OR REPLACE INTO `user_points_new` (`userId`, `totalPoints`) SELECT 'guest', `totalPoints` FROM `user_points`");
            database.execSQL("DROP TABLE `user_points`");
            database.execSQL("ALTER TABLE `user_points_new` RENAME TO `user_points`");
        }
    };

    public abstract FonosDao fonosDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    AppDatabase.class,
                    "fonos_database"
            ).addMigrations(MIGRATION_10_11).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
