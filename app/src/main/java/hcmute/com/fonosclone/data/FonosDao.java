package hcmute.com.fonosclone.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FonosDao {
    @Insert
    void insertBook(Book book);

    @Insert
    void insertPodCourse(PodCourse podCourse);

    @Insert
    void insertListeningHistory(ListeningHistory history);

    @Query("SELECT COALESCE(SUM(listenedSeconds), 0) FROM listening_history")
    int getTotalListenedSeconds();

    @Query("SELECT COUNT(*) FROM books")
    int countBooks();

    @Query("UPDATE books SET isFavorite = :isFavorite WHERE id = :bookId")
    void setFavorite(int bookId, boolean isFavorite);

    @Query("SELECT * FROM books WHERE type = :type")
    List<Book> getBooksByType(String type);

    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    Book getBookById(int bookId);

    @Query("SELECT * FROM books WHERE isFavorite = 1")
    List<Book> getFavoriteBooks();

    @Query("SELECT * FROM pod_courses")
    List<PodCourse> getPodCourses();
}
