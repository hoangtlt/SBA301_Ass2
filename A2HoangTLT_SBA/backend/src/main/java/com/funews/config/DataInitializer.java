/**
 * @author HoangTLT
 */

package com.funews.config;

import com.funews.entity.*;
import com.funews.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true")
public class DataInitializer implements CommandLineRunner {
    private final AccountRepository accounts;
    private final CategoryRepository categories;
    private final NewsRepository news;
    private final PasswordEncoder encoder;
    public DataInitializer(AccountRepository accounts, CategoryRepository categories, NewsRepository news, PasswordEncoder encoder) {
        this.accounts = accounts; this.categories = categories; this.news = news; this.encoder = encoder;
    }
    @Override @Transactional
    public void run(String... args) {
        // Seed only a completely empty database. Restarting never restores removed sample data.
        if (accounts.count() != 0 || categories.count() != 0 || news.count() != 0) return;
        account("Quản trị viên", "admin@funews.com", 1, "Admin@123");
        var staff = account("Nguyễn Minh Anh", "staff@funews.com", 2, "Staff@123");
        account("Trần Hải Nam", "staff2@funews.com", 2, "Staff@123");
        var campus = category("Đời sống sinh viên", "Hoạt động, câu lạc bộ và cuộc sống trong khuôn viên trường.");
        var study = category("Học tập & nghiên cứu", "Thông tin học thuật, nghiên cứu và cơ hội học tập.");
        category("Sự kiện", "Những chương trình sắp diễn ra tại trường.");
        article("Ngày hội câu lạc bộ: kết nối những đam mê", "Một ngày để khám phá, gặp gỡ và tìm cộng đồng của bạn.",
                "Ngày hội câu lạc bộ chào đón sinh viên đến giao lưu cùng các câu lạc bộ học thuật, nghệ thuật và thể thao.\n\nSinh viên có thể tìm hiểu hoạt động, đăng ký thành viên và tham gia các buổi trải nghiệm. Đây là cơ hội để làm quen với những người bạn mới và phát triển kỹ năng ngoài lớp học.", campus, staff, 1, "Sinh viên", 2);
        article("Workshop xây dựng ứng dụng web với Spring Boot", "Từ những kiến thức Java đầu tiên đến một REST API đơn giản.",
                "Khoa Công nghệ thông tin tổ chức buổi thực hành xây dựng ứng dụng web với Spring Boot và React.\n\nNội dung gồm thiết kế cơ sở dữ liệu, xây dựng REST API và kết nối giao diện. Sinh viên nên chuẩn bị máy tính đã cài Java, Node.js và công cụ lập trình.", study, staff, 1, "Công nghệ", 1);
        article("Kế hoạch hoạt động học kỳ mới", "Bản nháp đang chờ hoàn thiện thông tin.",
                "Nội dung kế hoạch đang được cập nhật. Bài viết này ở trạng thái ngừng hiển thị và chỉ Staff mới xem được trong trang quản lý.", campus, staff, 0, "Kế hoạch", 0);
    }
    private SystemAccount account(String name, String email, int role, String password) {
        var a = new SystemAccount();
        a.setAccountName(name); a.setAccountEmail(email); a.setAccountRole(role); a.setAccountPassword(encoder.encode(password));
        return accounts.save(a);
    }
    private Category category(String name, String description) {
        var c = new Category(); c.setCategoryName(name); c.setCategoryDescription(description); c.setIsActive(true);
        return categories.save(c);
    }
    private void article(String title, String headline, String content, Category category, SystemAccount author, int status, String tagName, int daysAgo) {
        var n = new NewsArticle();
        n.setNewsTitle(title); n.setHeadline(headline); n.setNewsContent(content); n.setNewsSource("Phòng Công tác sinh viên");
        n.setCategory(category); n.setCreatedBy(author); n.setNewsStatus(status); n.setCreatedDate(LocalDateTime.now().minusDays(daysAgo));
        var tag = new Tag(); tag.setTagName(tagName); tag.setNote("Tin mẫu"); n.getTags().add(tag);
        news.save(n);
    }
}
