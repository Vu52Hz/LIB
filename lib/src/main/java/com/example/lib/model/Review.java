package com.example.lib.model;

import jakarta.persistence.*;

@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reviewer;  // tên người đánh giá
    private String content;   // nội dung đánh giá

    @ManyToOne
    @JoinColumn(name = "book_id") // Mỗi review thuộc về 1 cuốn sách
    private Book book;

    // Getter, Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReviewer() { return reviewer; }
    public void setReviewer(String reviewer) { this.reviewer = reviewer; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }
}
