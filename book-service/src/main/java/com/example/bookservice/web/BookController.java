package com.example.bookservice.web;

import com.example.bookservice.domain.Book;
import com.example.bookservice.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookService service;

    public BookController(BookService service) { this.service = service; }

    @GetMapping
    public List<Book> all() { return service.all(); }

    @GetMapping("/{id}")
    public Book getById(@PathVariable long id) {
        return service.findById(id);
    }

    @PostMapping
    public Book create(@RequestBody Book book) {
        return service.save(book);
    }

    @PutMapping("/{id}")
    public Book update(@PathVariable long id, @RequestBody Book book) {
        book.setId(id);
        return service.save(book);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable long id) {
        service.delete(id);
    }

    @PostMapping("/init")
    public void init() { service.initDb(); }

    @PostMapping("/{id}/borrow")
    public BookService.BorrowResult borrow(@PathVariable long id) {
        return service.borrow(id);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}
