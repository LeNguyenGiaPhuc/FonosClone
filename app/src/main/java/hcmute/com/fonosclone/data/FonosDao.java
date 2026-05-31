package hcmute.com.fonosclone.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FonosDao {
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertListeningProgress(ListeningProgress progress);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertDownloadedContent(DownloadedContent content);

    @Query("SELECT COALESCE(SUM(listenedSeconds), 0) FROM listening_history")
    int getTotalListenedSeconds();

    @Query("SELECT * FROM listening_progress WHERE bookId = :bookId LIMIT 1")
    ListeningProgress getListeningProgress(int bookId);

    @Query("SELECT * FROM listening_progress ORDER BY updatedAt DESC LIMIT 1")
    ListeningProgress getLatestListeningProgress();

    @Query("SELECT COUNT(*) FROM books")
    int countBooks();

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    void setFavorite(int bookId, boolean isFavorite);

    @Query("UPDATE books SET isFavorite = 0")
    void clearBookFavorites();

    @Query("SELECT * FROM books WHERE type = :type")
    List<Book> getBooksByType(String type);

    @Query("SELECT * FROM books")
    List<Book> getAllBooks();

    @Query("SELECT * FROM books WHERE title LIKE '%' || :keyword || '%' OR author LIKE '%' || :keyword || '%'")
    List<Book> searchBooks(String keyword);

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(int bookId);

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    List<Book> getFavoriteBooks();

    @Query("SELECT books.* FROM books INNER JOIN downloaded_content ON books.id = downloaded_content.bookId ORDER BY downloaded_content.downloadedAt DESC")
    List<Book> getDownloadedBooks();

    @Query("SELECT COUNT(*) FROM downloaded_content WHERE bookId = :bookId")
    int isBookDownloaded(int bookId);

    @Query("SELECT * FROM pod_courses")
    List<PodCourse> getPodCourses();

    @Query("DELETE FROM books")
    void deleteAllBooks();

    @Query("DELETE FROM pod_courses")
    void deleteAllPodCourses();
}
