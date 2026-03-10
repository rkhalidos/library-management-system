package com.library.service;

import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.PatronRepository;
import com.library.util.LibraryLogger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for generating book recommendations.
 * Uses patron borrowing history and preferences to suggest books.
 */
public class RecommendationService {
    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final LoanRepository loanRepository;

    public RecommendationService(BookRepository bookRepository, PatronRepository patronRepository,
                                  LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.patronRepository = patronRepository;
        this.loanRepository = loanRepository;
    }

    /**
     * Gets book recommendations for a patron based on their borrowing history and preferences.
     *
     * @param patronId     Patron ID
     * @param maxResults   Maximum number of recommendations
     * @return List of recommended books
     */
    public List<Book> getRecommendations(String patronId, int maxResults) {
        Optional<Patron> patronOpt = patronRepository.findById(patronId);
        if (!patronOpt.isPresent()) {
            return new ArrayList<>();
        }

        Patron patron = patronOpt.get();
        Set<String> borrowedIsbns = getBorrowedBookIsbns(patronId);
        
        // Calculate scores for each available book
        Map<Book, Double> bookScores = new HashMap<>();
        List<Book> availableBooks = bookRepository.findAvailable();

        for (Book book : availableBooks) {
            // Skip books already borrowed
            if (borrowedIsbns.contains(book.getIsbn())) {
                continue;
            }

            double score = calculateRecommendationScore(patron, book, borrowedIsbns);
            if (score > 0) {
                bookScores.put(book, score);
            }
        }

        // Sort by score and return top results
        List<Book> recommendations = bookScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        LibraryLogger.info(LibraryLogger.SYSTEM, 
            String.format("Generated %d recommendations for patron %s", recommendations.size(), patronId));

        return recommendations;
    }

    /**
     * Gets book recommendations for a patron (default 5 results).
     *
     * @param patronId Patron ID
     * @return List of recommended books
     */
    public List<Book> getRecommendations(String patronId) {
        return getRecommendations(patronId, 5);
    }

    /**
     * Gets recommendations based on a specific genre.
     *
     * @param genre      Genre to base recommendations on
     * @param patronId   Patron ID (to exclude already borrowed books)
     * @param maxResults Maximum number of recommendations
     * @return List of recommended books
     */
    public List<Book> getRecommendationsByGenre(String genre, String patronId, int maxResults) {
        Set<String> borrowedIsbns = getBorrowedBookIsbns(patronId);

        return bookRepository.findByGenre(genre).stream()
                .filter(book -> book.isAvailable())
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Gets recommendations based on a specific author.
     *
     * @param author     Author to base recommendations on
     * @param patronId   Patron ID (to exclude already borrowed books)
     * @param maxResults Maximum number of recommendations
     * @return List of recommended books
     */
    public List<Book> getRecommendationsByAuthor(String author, String patronId, int maxResults) {
        Set<String> borrowedIsbns = getBorrowedBookIsbns(patronId);

        return bookRepository.findByAuthor(author).stream()
                .filter(book -> book.isAvailable())
                .filter(book -> !borrowedIsbns.contains(book.getIsbn()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Gets similar books based on a given book.
     *
     * @param isbn       ISBN of the reference book
     * @param maxResults Maximum number of similar books
     * @return List of similar books
     */
    public List<Book> getSimilarBooks(String isbn, int maxResults) {
        Optional<Book> bookOpt = bookRepository.findById(isbn);
        if (!bookOpt.isPresent()) {
            return new ArrayList<>();
        }

        Book referenceBook = bookOpt.get();
        List<Book> allBooks = bookRepository.findAll();

        // Calculate similarity scores
        Map<Book, Double> similarityScores = new HashMap<>();
        
        for (Book book : allBooks) {
            if (book.getIsbn().equals(isbn)) {
                continue;
            }

            double similarity = calculateSimilarity(referenceBook, book);
            if (similarity > 0) {
                similarityScores.put(book, similarity);
            }
        }

        return similarityScores.entrySet().stream()
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Gets popular books based on borrowing frequency.
     *
     * @param maxResults Maximum number of books
     * @return List of popular books
     */
    public List<Book> getPopularBooks(int maxResults) {
        // Count borrows for each book
        Map<String, Long> borrowCounts = loanRepository.findAll().stream()
                .collect(Collectors.groupingBy(LoanRecord::getBookIsbn, Collectors.counting()));

        // Sort by borrow count and get top books
        return borrowCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(maxResults)
                .map(e -> bookRepository.findById(e.getKey()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Gets recently added books.
     *
     * @param maxResults Maximum number of books
     * @return List of recently added books
     */
    public List<Book> getRecentlyAddedBooks(int maxResults) {
        // Since we don't track add date, return books sorted by publication year
        return bookRepository.findAll().stream()
                .sorted((b1, b2) -> Integer.compare(b2.getPublicationYear(), b1.getPublicationYear()))
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    /**
     * Gets trending genres based on recent borrowing activity.
     *
     * @param topN Number of top genres to return
     * @return Map of genre to borrow count
     */
    public Map<String, Long> getTrendingGenres(int topN) {
        // Count borrows by genre
        Map<String, Long> genreCounts = new HashMap<>();
        
        for (LoanRecord loan : loanRepository.findAll()) {
            bookRepository.findById(loan.getBookIsbn()).ifPresent(book -> {
                String genre = book.getGenre();
                if (genre != null) {
                    genreCounts.merge(genre, 1L, Long::sum);
                }
            });
        }

        // Sort and limit
        return genreCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    /**
     * Calculates the recommendation score for a book based on patron profile.
     *
     * @param patron        The patron
     * @param book          The book to score
     * @param borrowedIsbns ISBNs of books already borrowed
     * @return Recommendation score (higher is better)
     */
    private double calculateRecommendationScore(Patron patron, Book book, Set<String> borrowedIsbns) {
        double score = 0.0;
        
        // Genre preference match
        Set<String> preferences = patron.getPreferences();
        if (book.getGenre() != null && preferences.contains(book.getGenre().toLowerCase())) {
            score += 3.0;
        }

        // Author preference match
        if (preferences.contains(book.getAuthor().toLowerCase())) {
            score += 2.5;
        }

        // Similar to previously borrowed books
        for (String borrowedIsbn : borrowedIsbns) {
            bookRepository.findById(borrowedIsbn).ifPresent(borrowedBook -> {
                // Bonus if same genre
                if (book.getGenre() != null && book.getGenre().equals(borrowedBook.getGenre())) {
                    // Use a local variable to track the bonus
                }
                // Bonus if same author
                if (book.getAuthor().equals(borrowedBook.getAuthor())) {
                    // Use a local variable to track the bonus
                }
            });
        }

        // Popularity bonus (based on total borrows)
        long borrowCount = loanRepository.findByBookIsbn(book.getIsbn()).size();
        score += Math.min(borrowCount * 0.1, 1.0); // Cap at 1.0

        // Recency bonus (newer books get slight boost)
        int currentYear = java.time.Year.now().getValue();
        int yearDiff = currentYear - book.getPublicationYear();
        if (yearDiff <= 2) {
            score += 0.5;
        } else if (yearDiff <= 5) {
            score += 0.25;
        }

        return score;
    }

    /**
     * Calculates similarity between two books.
     *
     * @param book1 First book
     * @param book2 Second book
     * @return Similarity score (0.0 to 1.0)
     */
    private double calculateSimilarity(Book book1, Book book2) {
        double similarity = 0.0;

        // Same genre
        if (book1.getGenre() != null && book1.getGenre().equals(book2.getGenre())) {
            similarity += 0.4;
        }

        // Same author
        if (book1.getAuthor().equals(book2.getAuthor())) {
            similarity += 0.4;
        }

        // Similar publication year (within 5 years)
        int yearDiff = Math.abs(book1.getPublicationYear() - book2.getPublicationYear());
        if (yearDiff <= 5) {
            similarity += 0.2 * (1 - yearDiff / 5.0);
        }

        return similarity;
    }

    /**
     * Gets ISBNs of all books borrowed by a patron.
     *
     * @param patronId Patron ID
     * @return Set of borrowed ISBNs
     */
    private Set<String> getBorrowedBookIsbns(String patronId) {
        return loanRepository.findByPatronId(patronId).stream()
                .map(LoanRecord::getBookIsbn)
                .collect(Collectors.toSet());
    }

    /**
     * Gets borrowing statistics for a patron.
     *
     * @param patronId Patron ID
     * @return Map with borrowing statistics
     */
    public Map<String, Object> getPatronBorrowingStats(String patronId) {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        List<LoanRecord> loans = loanRepository.findByPatronId(patronId);
        stats.put("totalBorrows", loans.size());
        
        // Count by genre
        Map<String, Long> genreCounts = new HashMap<>();
        for (LoanRecord loan : loans) {
            bookRepository.findById(loan.getBookIsbn()).ifPresent(book -> {
                String genre = book.getGenre();
                if (genre != null) {
                    genreCounts.merge(genre, 1L, Long::sum);
                }
            });
        }
        stats.put("genreDistribution", genreCounts);

        // Count by author
        Map<String, Long> authorCounts = new HashMap<>();
        for (LoanRecord loan : loans) {
            bookRepository.findById(loan.getBookIsbn()).ifPresent(book -> {
                authorCounts.merge(book.getAuthor(), 1L, Long::sum);
            });
        }
        stats.put("authorDistribution", authorCounts);

        // Favorite genre
        String favoriteGenre = genreCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        stats.put("favoriteGenre", favoriteGenre);

        // Favorite author
        String favoriteAuthor = authorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        stats.put("favoriteAuthor", favoriteAuthor);

        return stats;
    }
}
