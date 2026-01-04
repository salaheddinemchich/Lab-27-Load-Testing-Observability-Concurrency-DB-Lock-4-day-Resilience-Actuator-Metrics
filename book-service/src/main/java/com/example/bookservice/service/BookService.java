package com.example.bookservice.service;

import com.example.bookservice.domain.Book;
import com.example.bookservice.repo.BookRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {
    private final BookRepository repo;
    private final PricingClient pricing;

    public BookService(BookRepository repo, PricingClient pricing) {
        this.repo = repo;
        this.pricing = pricing;
    }

    @Transactional
    public void initDb() {
        if(repo.count() == 0) {
            repo.save(new Book("Dune", "Herbert", 10));
            repo.save(new Book("1984", "Orwell", 2));
        }
    }

    public List<Book> all() { return repo.findAll(); }

    public Book findById(long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable avec l'id: " + id));
    }

    @Transactional
    public Book save(Book book) {
        return repo.save(book);
    }

    @Transactional
    public void delete(long id) {
        repo.deleteById(id);
    }

    @Transactional
    public BorrowResult borrow(long id) {
        // Le Lock DB se fait ici
        Book book = repo.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Livre introuvable"));
        
        book.decrementStock();
        
        // Appel externe (si échoue, le fallback prend le relais, la transaction DB n'est pas annulée)
        double price = pricing.getPrice(id);
        
        return new BorrowResult(book.getTitle(), book.getStock(), price);
    }

    public record BorrowResult(String title, int stockLeft, double price) {}
}
