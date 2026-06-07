package hcmute.com.fonosclone.data.local;

import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.ChallengeCompletion;
import hcmute.com.fonosclone.data.model.DownloadedContent;
import hcmute.com.fonosclone.data.model.FavoriteContent;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.ListeningProgress;
import hcmute.com.fonosclone.data.model.PodCourse;
import hcmute.com.fonosclone.data.model.UserPoints;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FonosDao {
    String BOOK_SELECT_FOR_USER =
            "SELECT b.id, b.title, b.author, b.type, b.coverImage, b.audioResName, " +
                    "b.audioUrl, b.audioStoragePath, " +
                    "CASE WHEN f.bookId IS NULL THEN 0 ELSE 1 END AS isFavorite, " +
                    "b.category " +
                    "FROM books b " +
                    "LEFT JOIN favorite_content f ON f.bookId = b.id AND f.userId = :userId ";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBook(Book book);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertBooks(List<Book> books);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPodCourse(PodCourse podCourse);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPodCourses(List<PodCourse> podCourses);

    @Insert
    void insertListeningHistory(ListeningHistory history);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertChallengeCompletion(ChallengeCompletion completion);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertUserPoints(UserPoints points);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertListeningProgress(ListeningProgress progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertDownloadedContent(DownloadedContent content);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertFavoriteContent(FavoriteContent favorite);

    @Query("DELETE FROM favorite_content WHERE userId = :userId AND bookId = :bookId")
    void deleteFavoriteContent(String userId, int bookId);

    @Query("DELETE FROM favorite_content WHERE userId = :userId")
    void clearFavoriteContentForUser(String userId);

    @Query("SELECT COALESCE(SUM(listenedSeconds), 0) FROM listening_history WHERE userId = :userId")
    int getTotalListenedSeconds(String userId);

    @Query("SELECT COALESCE(SUM(listenedSeconds), 0) FROM listening_history WHERE userId = :userId AND listenedAt BETWEEN :startMillis AND :endMillis")
    int getListenedSecondsBetween(String userId, long startMillis, long endMillis);

    @Query("SELECT * FROM listening_history WHERE userId = :userId AND listenedAt BETWEEN :startMillis AND :endMillis")
    List<ListeningHistory> getListeningHistoryBetween(String userId, long startMillis, long endMillis);

    @Query("SELECT * FROM listening_history WHERE userId = :userId ORDER BY listenedAt DESC")
    List<ListeningHistory> getListeningHistoryForUser(String userId);

    @Query("SELECT COUNT(*) FROM listening_progress WHERE userId = :userId AND durationMs > 0 AND positionMs >= durationMs * 9 / 10 AND updatedAt BETWEEN :startMillis AND :endMillis")
    int countCompletedBooksBetween(String userId, long startMillis, long endMillis);

    @Query("SELECT COUNT(*) FROM challenge_completions WHERE userId = :userId AND missionId = :missionId AND periodKey = :periodKey")
    int isMissionCompleted(String userId, String missionId, String periodKey);

    @Query("SELECT COALESCE(SUM(pointsAwarded), 0) FROM challenge_completions WHERE userId = :userId")
    int getTotalAwardedPoints(String userId);

    @Query("SELECT * FROM challenge_completions WHERE userId = :userId")
    List<ChallengeCompletion> getChallengeCompletions(String userId);

    @Query("SELECT * FROM user_points WHERE userId = :userId LIMIT 1")
    UserPoints getUserPoints(String userId);

    @Query("SELECT * FROM listening_progress WHERE userId = :userId AND bookId = :bookId LIMIT 1")
    ListeningProgress getListeningProgress(String userId, int bookId);

    @Query("SELECT * FROM listening_progress WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    ListeningProgress getLatestListeningProgress(String userId);

    @Query("SELECT * FROM listening_progress WHERE userId = :userId")
    List<ListeningProgress> getAllListeningProgress(String userId);

    @Query("SELECT COUNT(*) FROM books")
    int countBooks();

    @Query(BOOK_SELECT_FOR_USER + "WHERE b.type = :type")
    List<Book> getBooksByTypeForUser(String type, String userId);

    @Query(BOOK_SELECT_FOR_USER)
    List<Book> getAllBooksForUser(String userId);

    @Query(BOOK_SELECT_FOR_USER + "WHERE b.title LIKE '%' || :keyword || '%' OR b.author LIKE '%' || :keyword || '%'")
    List<Book> searchBooksForUser(String keyword, String userId);

    @Query(BOOK_SELECT_FOR_USER + "WHERE b.id = :bookId LIMIT 1")
    Book getBookByIdForUser(int bookId, String userId);

    @Query("SELECT * FROM books WHERE type = :type")
    List<Book> getBooksByType(String type);

    @Query("SELECT * FROM books")
    List<Book> getAllBooks();

    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%' OR author LIKE '%' || :keyword || '%'")
    List<Book> searchBooks(String keyword);

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(int bookId);

    @Query(BOOK_SELECT_FOR_USER + "WHERE f.bookId IS NOT NULL")
    List<Book> getFavoriteBooks(String userId);

    @Query(BOOK_SELECT_FOR_USER + "INNER JOIN downloaded_content d ON b.id = d.bookId AND d.userId = :userId ORDER BY d.downloadedAt DESC")
    List<Book> getDownloadedBooks(String userId);

    @Query("SELECT COUNT(*) FROM downloaded_content WHERE userId = :userId AND bookId = :bookId")
    int isBookDownloaded(String userId, int bookId);

    @Query("SELECT * FROM downloaded_content WHERE userId = :userId")
    List<DownloadedContent> getDownloadedContents(String userId);

    @Query("SELECT * FROM downloaded_content WHERE userId = :userId AND bookId = :bookId LIMIT 1")
    DownloadedContent getDownloadedContent(String userId, int bookId);

    @Query("SELECT * FROM pod_courses")
    List<PodCourse> getPodCourses();

    @Query("DELETE FROM books")
    void deleteAllBooks();

    @Query("DELETE FROM pod_courses")
    void deleteAllPodCourses();
}
