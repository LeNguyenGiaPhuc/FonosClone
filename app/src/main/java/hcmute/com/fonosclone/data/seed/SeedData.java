package hcmute.com.fonosclone.data.seed;


import hcmute.com.fonosclone.data.local.FonosDao;
import hcmute.com.fonosclone.data.model.Book;
import hcmute.com.fonosclone.data.model.ListeningHistory;
import hcmute.com.fonosclone.data.model.PodCourse;

public class SeedData {
    public static void insertSampleData(FonosDao dao) {
        if (dao.countBooks() > 0) {
            return;
        }

        // AUDIOBOOK
        dao.insertBook(new Book("Hậu Hồng Lâu Mộng", "Tào Tuyết Cần", "AUDIOBOOK", "hau_hong_lau_mong", "demo_audio", true, "Văn học & Truyện"));
        dao.insertBook(new Book("Tại sao người thông minh dễ tổn thương", "Đặng cập nhật", "AUDIOBOOK", "tai_sao_nguoi_thong_minh_de_ton_thuong", "demo_audio", false, "Tâm lý học"));
        dao.insertBook(new Book("Bông hồng nhung", "Nhiều tác giả", "AUDIOBOOK", "bong_hong_nhung", "demo_audio", false, "Văn học & Truyện"));
        dao.insertBook(new Book("Những người khốn khổ", "Victor Hugo", "AUDIOBOOK", "nhung_nguoi_khon_kho", "demo_audio", true, "Văn học & Truyện"));
        dao.insertBook(new Book("Lá bài chủ", "Đặng cập nhật", "AUDIOBOOK", "la_bai_chu", "demo_audio", false, "Văn học & Truyện"));
        dao.insertBook(new Book("Bên kia bức tường", "Nguyễn Nhật Ánh", "AUDIOBOOK", "ben_kia_buc_tuong", "demo_audio", false, "Thiếu nhi"));
        dao.insertBook(new Book("Mùi hương", "Patrick Suskind", "AUDIOBOOK", "mui_huong", "demo_audio", false, "Văn học & Truyện"));
        dao.insertBook(new Book("Yên huyết", "Đặng cập nhật", "AUDIOBOOK", "yen_huyet", "demo_audio", false, "Văn học & Truyện"));
        dao.insertBook(new Book("Phi vụ cuối", "Đặng cập nhật", "AUDIOBOOK", "phi_vu_cuoi", "demo_audio", false, "Văn học & Truyện"));

        // EBOOK
        dao.insertBook(new Book("Ra quyết định thông minh", "Đặng cập nhật", "EBOOK", "ra_quyet_dinh_thong_minh", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Khéo ăn nói được thiên hạ", "Trác Nhã", "EBOOK", "kheo_an_noi_duok_thien_ha", "demo_audio", false, "Kỹ năng sống"));
        dao.insertBook(new Book("Sức bật trong sự nghiệp", "Đặng cập nhật", "EBOOK", "suc_bat_trong_su_nghiep", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Xây dựng đội nhóm hiệu suất cao", "Đặng cập nhật", "EBOOK", "xay_dung_doi_nhom_hieu_suat_cao", "demo_audio", true, "Kinh doanh"));
        dao.insertBook(new Book("Stand Out - Khác Biệt", "Đặng cập nhật", "EBOOK", "stand_out_khac_biet", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Lần đầu làm sếp", "Nam Nguyễn", "EBOOK", "lan_au_lam_sep", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Pippi tất dài", "Astrid Lindgren", "EBOOK", "pippi_tat_dai", "demo_audio", false, "Thiếu nhi"));
        dao.insertBook(new Book("Chú bé có tài mở khóa", "Nguyễn Nhật Ánh", "EBOOK", "chu_be_co_tai_mo_khoa", "demo_audio", false, "Thiếu nhi"));

        // SUMMARY
        dao.insertBook(new Book("Nghĩ giàu và làm giàu", "Napoleon Hill", "SUMMARY", "nghi_giau_va_lam_giau", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Dịch vụ sững sờ, khách hàng sững sờ", "Đặng cập nhật", "SUMMARY", "dich_vu_sung_sot_khach_hang_sung_so", "demo_audio", false, "Kinh doanh"));
        dao.insertBook(new Book("Luyện trí nhớ", "Đặng cập nhật", "SUMMARY", "luyen_tri_nho", "demo_audio", false, "Kỹ năng sống"));
        dao.insertBook(new Book("Papillon người tù khổ sai", "Đặng cập nhật", "SUMMARY", "papillon_nguoi_tu_kho_sai", "demo_audio", false, "Văn học & Truyện"));
        dao.insertBook(new Book("Phi lý một cách hợp lý", "Dan Ariely", "SUMMARY", "phi_ly_mot_cach_hop_ly", "demo_audio", false, "Tâm lý học"));
        dao.insertBook(new Book("Quảng Trị 1972", "Đặng cập nhật", "SUMMARY", "quang_tri_1972", "demo_audio", false, "Văn học & Truyện"));

        // PODCOURSE
        dao.insertPodCourse(new PodCourse("AI for Beginners", "Nam Nguyễn", "Technology", "#1E8080", 4.8));
        dao.insertPodCourse(new PodCourse("Management for First-Time Leaders", "Vũ Đức Trí", "Management", "#7A5540", 4.7));
        dao.insertPodCourse(new PodCourse("Personal Finance Thinking", "Trần Việt Quân", "Finance", "#1A3A5C", 4.7));
        dao.insertPodCourse(new PodCourse("Kỹ năng Thuyết trình Chuyên nghiệp", "Nhiều tác giả", "Soft Skills", "#5A3E91", 4.6));
        dao.insertPodCourse(new PodCourse("Startup 101: Từ Ý tưởng đến Gọi vốn", "Lâm Minh Chánh", "Business", "#A63B3B", 4.9));
        dao.insertPodCourse(new PodCourse("Product Management Đột phá", "Hoàng Nam Tiến", "Product", "#2E6930", 4.8));

        dao.insertListeningHistory(new ListeningHistory(1, 0));
    }
}
