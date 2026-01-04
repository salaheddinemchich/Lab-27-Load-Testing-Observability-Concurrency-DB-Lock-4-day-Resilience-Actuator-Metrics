package com.example.bookservice.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "book")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private int stock;

    public Book() {}
    public Book(String title, String author, int stock) {
        this.title = title; this.author = author; this.stock = stock;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public void decrementStock() {
        if (stock <= 0) throw new IllegalStateException("Plus d’exemplaires");
        stock--;
    }
}
