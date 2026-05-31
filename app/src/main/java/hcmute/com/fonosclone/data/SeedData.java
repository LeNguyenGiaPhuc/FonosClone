package hcmute.com.fonosclone.data;

public class SeedData {
    public static void insertSampleData(FonosDao dao) {
        if (dao.countBooks() > 0) {
            return;
        }

        dao.insertBook(new Book("Hau hong lau mong", "Tao Tuyet Can", "AUDIOBOOK", "hau_hong_lau_mong", "demo_audio", true));
        dao.insertBook(new Book("Tai sao nguoi thong minh de ton thuong", "Dang cap nhat", "AUDIOBOOK", "tai_sao_nguoi_thong_minh_de_ton_thuong", "demo_audio", false));
        dao.insertBook(new Book("Bong hong nhung", "Nhiều tác giả", "AUDIOBOOK", "bong_hong_nhung", "demo_audio", false));
        dao.insertBook(new Book("Nhung nguoi khon kho", "Victor Hugo", "AUDIOBOOK", "nhung_nguoi_khon_kho", "demo_audio", true));
        dao.insertBook(new Book("La bai chu", "Dang cap nhat", "AUDIOBOOK", "la_bai_chu", "demo_audio", false));
        dao.insertBook(new Book("Hoa tuong vi trong em", "Dang cap nhat", "AUDIOBOOK", "hoa_tuong_vi_trong_em", "demo_audio", false));
        dao.insertBook(new Book("Ia o di cot", "Dang cap nhat", "AUDIOBOOK", "ia_o_di_cot", "demo_audio", false));
        dao.insertBook(new Book("Vuot con ao", "Dang cap nhat", "AUDIOBOOK", "vuot_con_ao", "demo_audio", false));
        dao.insertBook(new Book("I tim ba ngoai", "Dang cap nhat", "AUDIOBOOK", "i_tim_ba_ngoai", "demo_audio", false));

        dao.insertBook(new Book("Ra quyet dinh thong minh", "Dang cap nhat", "EBOOK", "ra_quyet_dinh_thong_minh", "demo_audio", false));
        dao.insertBook(new Book("Khi vet thuong nam xuong", "Dang cap nhat", "EBOOK", "khi_vet_thuong_nam_xuong", "demo_audio", false));
        dao.insertBook(new Book("Cay phong non chum khan o", "Dang cap nhat", "EBOOK", "cay_phong_non_chum_khan_o", "demo_audio", false));
        dao.insertBook(new Book("Phia sau vu an nguoi xa la", "Dang cap nhat", "EBOOK", "phia_sau_vu_an_nguoi_xa_la", "demo_audio", false));
        dao.insertBook(new Book("Hoang ha nho hong ha thuong", "Dang cap nhat", "EBOOK", "hoang_ha_nho_hong_ha_thuong", "demo_audio", false));
        dao.insertBook(new Book("Thanh pho sau anh hao quang", "Dang cap nhat", "EBOOK", "thanh_pho_sau_anh_hao_quang", "demo_audio", false));
        dao.insertBook(new Book("Chuyen nui oi va thao nguyen", "Dang cap nhat", "EBOOK", "chuyen_nui_oi_va_thao_nguyen", "demo_audio", false));
        dao.insertBook(new Book("Ket thuc ban hang on quyet dinh", "Dang cap nhat", "EBOOK", "ket_thuc_ban_hang_on_quyet_inh", "demo_audio", false));
        dao.insertBook(new Book("Xay dung doi nhom hieu suat cao", "Dang cap nhat", "EBOOK", "xay_dung_doi_nhom_hieu_suat_cao", "demo_audio", false));
        dao.insertBook(new Book("Ke thanh cong phai biet lang nghe", "Dang cap nhat", "EBOOK", "ke_thanh_cong_phai_biet_lang_nghe", "demo_audio", false));

        dao.insertBook(new Book("Tro lai ia ang", "Dang cap nhat", "SUMMARY", "tro_lai_ia_ang", "demo_audio", false));
        dao.insertBook(new Book("Mac ke no lam toi i", "Dang cap nhat", "SUMMARY", "mac_ke_no_lam_toi_i", "demo_audio", false));
        dao.insertBook(new Book("Tinh the hiem ngheo", "Dang cap nhat", "SUMMARY", "tinh_the_hiem_ngheo", "demo_audio", false));
        dao.insertBook(new Book("Nha tien tri cuoi cung", "Dang cap nhat", "SUMMARY", "nha_tien_tri_cuoi_cung", "demo_audio", false));
        dao.insertBook(new Book("Truyen thong giao tiep", "Dang cap nhat", "SUMMARY", "truyen_thong_giao_tiep", "demo_audio", false));
        dao.insertBook(new Book("Ben kia buc tuong", "Nguyen Nhat Anh", "AUDIOBOOK", "ben_kia_buc_tuong", "demo_audio", false));
        dao.insertBook(new Book("Mui huong", "Patrick Suskind", "AUDIOBOOK", "mui_huong", "demo_audio", false));
        dao.insertBook(new Book("Yen huyet", "Dang cap nhat", "AUDIOBOOK", "yen_huyet", "demo_audio", false));
        dao.insertBook(new Book("Phi vu cuoi", "Dang cap nhat", "AUDIOBOOK", "phi_vu_cuoi", "demo_audio", false));

        dao.insertBook(new Book("Ra quyet dinh thong minh", "Dang cap nhat", "EBOOK", "ra_quyet_dinh_thong_minh", "demo_audio", false));
        dao.insertBook(new Book("Kheo an noi duoc thien ha", "Trac Nha", "EBOOK", "kheo_an_noi_duoc_thien_ha", "demo_audio", false));
        dao.insertBook(new Book("Suc bat trong su nghiep", "Dang cap nhat", "EBOOK", "suc_bat_trong_su_nghiep", "demo_audio", false));
        dao.insertBook(new Book("Xay dung doi nhom hieu suat cao", "Dang cap nhat", "EBOOK", "xay_dung_doi_nhom_hieu_suat_cao", "demo_audio", true));
        dao.insertBook(new Book("Stand Out - Khac Biet", "Dang cap nhat", "EBOOK", "stand_out_khac_biet", "demo_audio", false));
        dao.insertBook(new Book("Lan dau lam sep", "Nam Nguyen", "EBOOK", "lan_au_lam_sep", "demo_audio", false));
        dao.insertBook(new Book("Pippi tat dai", "Astrid Lindgren", "EBOOK", "pippi_tat_dai", "demo_audio", false));
        dao.insertBook(new Book("Chu be co tai mo khoa", "Nguyen Nhat Anh", "EBOOK", "chu_be_co_tai_mo_khoa", "demo_audio", false));

        dao.insertBook(new Book("Nghi giau va lam giau", "Napoleon Hill", "SUMMARY", "nghi_giau_va_lam_giau", "demo_audio", false));
        dao.insertBook(new Book("Dich vu sung sot, khach hang sung so", "Dang cap nhat", "SUMMARY", "dich_vu_sung_sot_khach_hang_sung_so", "demo_audio", false));
        dao.insertBook(new Book("Luyen tri nho", "Dang cap nhat", "SUMMARY", "luyen_tri_nho", "demo_audio", false));
        dao.insertBook(new Book("Papillon nguoi tu kho sai", "Dang cap nhat", "SUMMARY", "papillon_nguoi_tu_kho_sai", "demo_audio", false));
        dao.insertBook(new Book("Phi ly mot cach hop ly", "Dan Ariely", "SUMMARY", "phi_ly_mot_cach_hop_ly", "demo_audio", false));
        dao.insertBook(new Book("Quang Tri 1972", "Dang cap nhat", "SUMMARY", "quang_tri_1972", "demo_audio", false));


        dao.insertPodCourse(new PodCourse("AI for Beginners", "Nam Nguyen", "Technology", "#1E8080", 4.8));
        dao.insertPodCourse(new PodCourse("Management for First-Time Leaders", "Vu Duc Tri", "Management", "#7A5540", 4.7));
        dao.insertPodCourse(new PodCourse("Personal Finance Thinking", "Tran Viet Quan", "Finance", "#1A3A5C", 4.7));
        dao.insertPodCourse(new PodCourse("Kỹ năng Thuyết trình Chuyên nghiệp", "Nhiều tác giả", "Soft Skills", "#5A3E91", 4.6));
        dao.insertPodCourse(new PodCourse("Startup 101: Từ Ý tưởng đến Gọi vốn", "Lâm Minh Chánh", "Business", "#A63B3B", 4.9));
        dao.insertPodCourse(new PodCourse("Product Management Đột phá", "Hoàng Nam Tiến", "Product", "#2E6930", 4.8));

        dao.insertListeningHistory(new ListeningHistory(1, 0));
    }
}
