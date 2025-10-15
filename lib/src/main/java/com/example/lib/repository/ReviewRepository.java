package com.example.lib.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.lib.model.Review;
import com.example.lib.model.Book;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Lấy danh sách review theo từng cuốn sách
    List<Review> findByBook(Book book);
    List<Review> findByBookId(Long bookId);
}
