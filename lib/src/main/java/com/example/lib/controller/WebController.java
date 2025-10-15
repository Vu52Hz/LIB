package com.example.lib.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.lib.model.Book;
import com.example.lib.model.User;
import com.example.lib.model.Review;
import com.example.lib.repository.BookRepository;
import com.example.lib.repository.UserRepository;
import com.example.lib.repository.ReviewRepository;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import java.util.Optional;

@Controller
public class WebController {
    // Khai báo các repository để truy cập dữ liệu
    private final BookRepository bookRepo;
    private final UserRepository userRepo;
    private final ReviewRepository reviewRepo;

    // Constructor để Spring Boot tự động tiêm (inject) các dependency
    public WebController(BookRepository bookRepo, UserRepository userRepo, ReviewRepository reviewRepo) {
        this.bookRepo = bookRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
    }

    // -------------------------------
    // ✅ TRANG GỐC (root "/")
    // -------------------------------
    @GetMapping("/")
    public String root(HttpSession session) {
        // Nếu người dùng đã đăng nhập -> chuyển đến /index
        if (session.getAttribute("user") != null) {
            return "redirect:/index";
        } 
        // Nếu chưa đăng nhập -> chuyển đến /login
        else {
            return "redirect:/login";
        }
    }

    // -------------------------------
    // ✅ TRANG CHÍNH (index)
    // -------------------------------
    @GetMapping("/index")
    public String index(HttpSession session) {
        // Nếu chưa đăng nhập -> quay lại trang login
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        // Nếu đã login -> hiển thị trang chính (index.html)
        return "index";
    }

    // -------------------------------
    // ✅ DANH SÁCH SÁCH
    // -------------------------------
    @GetMapping("/books")
    public String books(Model model) {
        // Lấy toàn bộ danh sách sách từ database
        model.addAttribute("books", bookRepo.findAll());
        // Trả về file Thymeleaf: books.html
        return "books";
    }

    // -------------------------------
    // ✅ FORM THÊM SÁCH MỚI
    // -------------------------------
    @GetMapping("/books/new")
    public String showAddForm(Model model) {
        // Tạo đối tượng book rỗng để gắn vào form nhập
        model.addAttribute("book", new Book());
        return "addbook"; // file addbook.html
    }

    // -------------------------------
    // ✅ LƯU SÁCH MỚI SAU KHI NHẬP
    // -------------------------------
    @PostMapping("/books")
    public String addBook(@ModelAttribute Book book) {
        // Lưu sách mới vào database
        bookRepo.save(book);
        // Sau khi thêm xong, quay lại danh sách sách
        return "redirect:/books";
    }

    // -------------------------------
    // ✅ TRANG CHI TIẾT MỘT CUỐN SÁCH
    // -------------------------------
    @GetMapping("/books/detail")
    public String bookDetail(@RequestParam Long id, Model model) {
        // Tìm sách theo ID
        Optional<Book> bookOpt = bookRepo.findById(id);
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            // Gửi dữ liệu sách sang giao diện
            model.addAttribute("book", book);
            // Lấy danh sách review của cuốn sách đó
            model.addAttribute("reviews", reviewRepo.findByBook(book));
            // Đối tượng review trống để gắn vào form thêm mới
            model.addAttribute("review", new Review());
            // Trả về giao diện chi tiết sách
            return "book_detail";
        }
        // Nếu không tìm thấy ID, quay về danh sách
        return "redirect:/books";
    }

    // -------------------------------
    // ✅ THÊM ĐÁNH GIÁ (REVIEW)
    // -------------------------------
    @PostMapping("/reviews/add")
    public String addReview(@RequestParam Long bookId,
                            @RequestParam String reviewer,
                            @RequestParam String content) {
        // Kiểm tra sách có tồn tại không
        Optional<Book> bookOpt = bookRepo.findById(bookId);
        if (bookOpt.isPresent()) {
            // Tạo đối tượng review mới
            Review review = new Review();
            review.setBook(bookOpt.get());
            review.setReviewer(reviewer);
            review.setContent(content);
            // Lưu review vào database
            reviewRepo.save(review);
        }
        // Sau khi thêm xong, quay lại trang chi tiết của sách
        return "redirect:/books/detail?id=" + bookId;
    }

    // -------------------------------
    // ✅ TRANG ĐĂNG NHẬP
    // -------------------------------
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    // -------------------------------
    // ✅ XỬ LÝ ĐĂNG NHẬP
    // -------------------------------
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {

        // Tìm user trong database theo username
        var u = userRepo.findByUsername(username);

        // Nếu có user và mật khẩu đúng
        if (u != null && u.getPassword().equals(password)) {
            // Lưu thông tin user vào session
            session.setAttribute("user", u);
            // Quay về trang chính
            return "redirect:/index";
        }

        // Nếu sai tài khoản/mật khẩu -> báo lỗi
        model.addAttribute("error", "Sai tài khoản hoặc mật khẩu");
        return "login";
    }

    // -------------------------------
    // ✅ TRANG ĐĂNG KÝ TÀI KHOẢN
    // -------------------------------
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // -------------------------------
    // ✅ XỬ LÝ ĐĂNG KÝ TÀI KHOẢN
    // -------------------------------
    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        // Kiểm tra xem username đã tồn tại chưa
        var existing = userRepo.findByUsername(username);
        if (existing != null) {
            model.addAttribute("error", "Tên đăng nhập đã tồn tại");
            return "register"; // Trả về lại form và báo lỗi
        }

        // Nếu chưa tồn tại -> tạo mới user
        var u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole("user"); // Mặc định role là 'user'
        userRepo.save(u);

        // Sau khi đăng ký -> quay lại trang login
        return "redirect:/login";
    }

    // -------------------------------
    // ✅ ĐĂNG XUẤT (LOGOUT)
    // -------------------------------
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // Xóa thông tin user trong session
        session.invalidate();
        // Quay lại trang đăng nhập
        return "redirect:/login";
    }
}
