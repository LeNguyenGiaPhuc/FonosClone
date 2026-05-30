package hcmute.com.fonosclone.data;

public class SeedData {
    public static void insertSampleData(FonosDao dao) {
        if (dao.countBooks() > 0) {
            return;
        }

        dao.insertBook(new Book("Hau hong lau mong", "Dang cap nhat", "AUDIOBOOK", "hau_hong_lau_mong", false));
        dao.insertBook(new Book("Tai sao nguoi thong minh de ton thuong", "Dang cap nhat", "AUDIOBOOK", "tai_sao_nguoi_thong_minh_de_ton_thuong", false));
        dao.insertBook(new Book("Bong hong nhung", "Dang cap nhat", "AUDIOBOOK", "bong_hong_nhung", false));
        dao.insertBook(new Book("Nhung nguoi khon kho", "Dang cap nhat", "AUDIOBOOK", "nhung_nguoi_khon_kho", false));
        dao.insertBook(new Book("La bai chu", "Dang cap nhat", "AUDIOBOOK", "la_bai_chu", false));
        dao.insertBook(new Book("Hoa tuong vi trong dem", "Dang cap nhat", "AUDIOBOOK", "hoa_tuong_vi_trong_dem", false));
        dao.insertBook(new Book("Ra quyet dinh thong minh", "Dang cap nhat", "AUDIOBOOK", "ra_quyet_dinh_thong_minh", false));
        dao.insertBook(new Book("Kheo an noi duoc thien ha", "Dang cap nhat", "AUDIOBOOK", "kheo_an_noi_duoc_thien_ha", false));
        dao.insertBook(new Book("Suc bat trong su nghiep", "Dang cap nhat", "AUDIOBOOK", "suc_bat_trong_su_nghiep", false));
        dao.insertBook(new Book("Xay dung doi nhom hieu suat cao", "Dang cap nhat", "AUDIOBOOK", "xay_dung_doi_nhom_hieu_suat_cao", false));
        dao.insertBook(new Book("Stand Out - Khac Biet", "Dang cap nhat", "AUDIOBOOK", "stand_out_khac_biet", false));
        dao.insertBook(new Book("Nghi giau va lam giau", "Dang cap nhat", "AUDIOBOOK", "nghi_giau_va_lam_giau", false));
        dao.insertBook(new Book("Dich vu sung sot, khach hang sung so", "Dang cap nhat", "AUDIOBOOK", "dich_vu_sung_sot_khach_hang_sung_so", false));
        dao.insertBook(new Book("Mui huong", "Dang cap nhat", "AUDIOBOOK", "mui_huong", false));
        dao.insertBook(new Book("Luyen tri nho", "Dang cap nhat", "AUDIOBOOK", "luyen_tri_nho", false));
        dao.insertBook(new Book("Papillon nguoi tu kho sai", "Dang cap nhat", "AUDIOBOOK", "papillon_nguoi_tu_kho_sai", false));

        dao.insertPodCourse(new PodCourse("AI for Beginners", "Nam Nguyen", "Technology", "#1E8080", 4.8));
        dao.insertPodCourse(new PodCourse("Management for First-Time Leaders", "Vu Duc Tri", "Management", "#7A5540", 4.7));

        dao.insertListeningHistory(new ListeningHistory(1, 0));
    }
}
