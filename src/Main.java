import com.library.factory.BookFactory;
import com.library.factory.PatronFactory;
import com.library.model.*;
import com.library.repository.*;
import com.library.service.*;
import com.library.util.LibraryLogger;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Main entry point for the Library Management System.
 * Provides a demonstration of all system features.
 */
public class Main {
    private static BookRepository bookRepository;
    private static PatronRepository patronRepository;
    private static LoanRepository loanRepository;
    
    private static BookService bookService;
    private static PatronService patronService;
    private static LendingService lendingService;
    private static ReservationService reservationService;
    private static RecommendationService recommendationService;

    public static void main(String[] args) {
        // Initialize the logger
        LibraryLogger.initialize();
        
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║       LIBRARY MANAGEMENT SYSTEM - Demo Application         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println();
        
        // Initialize repositories
        initializeRepositories();
        
        // Initialize services
        initializeServices();
        
        // Load sample data
        loadSampleData();
        
        // Run demo
        runDemo();
        
        // Interactive menu (optional)
        if (args.length > 0 && args[0].equals("--interactive")) {
            runInteractiveMenu();
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              Demo completed successfully!                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    private static void initializeRepositories() {
        System.out.println("► Initializing repositories...");
        bookRepository = new BookRepository();
        patronRepository = new PatronRepository();
        loanRepository = new LoanRepository();
        System.out.println("  ✓ Repositories initialized\n");
    }

    private static void initializeServices() {
        System.out.println("► Initializing services...");
        bookService = new BookService(bookRepository);
        patronService = new PatronService(patronRepository, loanRepository);
        lendingService = new LendingService(bookRepository, patronRepository, loanRepository);
        reservationService = new ReservationService(bookRepository, patronRepository);
        recommendationService = new RecommendationService(bookRepository, patronRepository, loanRepository);
        
        // Link services
        lendingService.setReservationService(reservationService);
        
        System.out.println("  ✓ Services initialized\n");
    }

    private static void loadSampleData() {
        System.out.println("► Loading sample data...\n");
        
        // Add sample books
        System.out.println("  Adding books to the library:");
        
        Book book1 = BookFactory.createSciFiBook("978-0-06-112008-4", "1984", "George Orwell", 1949);
        Book book2 = BookFactory.createFictionBook("978-0-452-28423-4", "To Kill a Mockingbird", "Harper Lee", 1960);
        Book book3 = BookFactory.createSciFiBook("978-0-553-38016-8", "Dune", "Frank Herbert", 1965);
        Book book4 = BookFactory.createMysteryBook("978-0-307-47427-8", "The Girl with the Dragon Tattoo", "Stieg Larsson", 2005);
        Book book5 = BookFactory.createTechnicalBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 2008);
        Book book6 = BookFactory.createTechnicalBook("978-0-201-63361-0", "Design Patterns", "Gang of Four", 1994);
        Book book7 = BookFactory.createFictionBook("978-0-7432-7356-5", "The Great Gatsby", "F. Scott Fitzgerald", 1925);
        Book book8 = BookFactory.createSciFiBook("978-0-441-17271-9", "Neuromancer", "William Gibson", 1984);
        
        bookService.addBook(book1);
        bookService.addBook(book2);
        bookService.addBook(book3);
        bookService.addBook(book4);
        bookService.addBook(book5);
        bookService.addBook(book6);
        bookService.addBook(book7);
        bookService.addBook(book8);
        
        System.out.println("  ✓ " + bookService.getTotalBookCount() + " books added\n");
        
        // Add sample patrons
        System.out.println("  Registering patrons:");
        
        Patron patron1 = PatronFactory.createPatronWithId("P001", "Alice Johnson", "alice@email.com", PatronType.REGULAR);
        Patron patron2 = PatronFactory.createPatronWithId("P002", "Bob Smith", "bob@email.com", PatronType.STUDENT);
        Patron patron3 = PatronFactory.createPatronWithId("P003", "Carol Williams", "carol@email.com", PatronType.FACULTY);
        
        // Add preferences
        patron1.addPreference("science fiction");
        patron1.addPreference("george orwell");
        patron2.addPreference("technical");
        patron2.addPreference("programming");
        patron3.addPreference("fiction");
        patron3.addPreference("mystery");
        
        patronService.addPatron(patron1);
        patronService.addPatron(patron2);
        patronService.addPatron(patron3);
        
        System.out.println("  ✓ " + patronService.getTotalPatronCount() + " patrons registered\n");
    }

    private static void runDemo() {
        System.out.println("════════════════════════════════════════════════════════════");
        System.out.println("                    SYSTEM DEMONSTRATION                     ");
        System.out.println("════════════════════════════════════════════════════════════\n");
        
        // Demo 1: Book Search
        demoBookSearch();
        
        // Demo 2: Book Checkout
        demoBookCheckout();
        
        // Demo 3: Book Return
        demoBookReturn();
        
        // Demo 4: Reservation System
        demoReservation();
        
        // Demo 5: Recommendations
        demoRecommendations();
        
        // Demo 6: System Statistics
        demoSystemStatistics();
    }

    private static void demoBookSearch() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 1: Book Search Functionality");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        // Search by title
        System.out.println("► Searching for books with 'code' in title:");
        List<Book> results = bookService.searchByTitle("code");
        printBookList(results);
        
        // Search by author
        System.out.println("► Searching for books by 'Orwell':");
        results = bookService.searchByAuthor("Orwell");
        printBookList(results);
        
        // Search by genre
        System.out.println("► Searching for Science Fiction books:");
        results = bookService.searchByGenre("Science Fiction");
        printBookList(results);
        
        // Composite search
        System.out.println("► Composite search for 'fiction':");
        results = bookService.searchBooks("fiction");
        printBookList(results);
        
        System.out.println();
    }

    private static void demoBookCheckout() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 2: Book Checkout");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        String patronId = "P001";
        String isbn = "978-0-06-112008-4"; // 1984
        
        System.out.println("► Patron " + patronId + " checking out book ISBN: " + isbn);
        
        try {
            LoanRecord loan = lendingService.checkoutBook(isbn, patronId);
            System.out.println("  ✓ Checkout successful!");
            System.out.println("    Loan ID: " + loan.getLoanId().substring(0, 8) + "...");
            System.out.println("    Due Date: " + loan.getDueDate());
            System.out.println("    Days to return: " + loan.getDaysUntilDue());
        } catch (Exception e) {
            System.out.println("  ✗ Checkout failed: " + e.getMessage());
        }
        
        // Checkout another book
        isbn = "978-0-553-38016-8"; // Dune
        System.out.println("\n► Patron " + patronId + " checking out book ISBN: " + isbn);
        
        try {
            LoanRecord loan = lendingService.checkoutBook(isbn, patronId);
            System.out.println("  ✓ Checkout successful!");
            System.out.println("    Due Date: " + loan.getDueDate());
        } catch (Exception e) {
            System.out.println("  ✗ Checkout failed: " + e.getMessage());
        }
        
        // Show patron's borrowed books
        System.out.println("\n► Active loans for patron " + patronId + ":");
        List<LoanRecord> activeLoans = lendingService.getPatronActiveLoans(patronId);
        for (LoanRecord loan : activeLoans) {
            Book book = bookService.getBookByIsbn(loan.getBookIsbn());
            System.out.println("    - " + book.getTitle() + " (Due: " + loan.getDueDate() + ")");
        }
        
        System.out.println();
    }

    private static void demoBookReturn() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 3: Book Return");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        String patronId = "P001";
        String isbn = "978-0-553-38016-8"; // Dune
        
        System.out.println("► Patron " + patronId + " returning book ISBN: " + isbn);
        
        try {
            LoanRecord loan = lendingService.returnBook(isbn, patronId);
            System.out.println("  ✓ Return successful!");
            System.out.println("    Return Date: " + loan.getReturnDate());
            System.out.println("    Days borrowed: " + loan.getDaysBorrowed());
        } catch (Exception e) {
            System.out.println("  ✗ Return failed: " + e.getMessage());
        }
        
        // Check book status
        Book book = bookService.getBookByIsbn(isbn);
        System.out.println("  Book status after return: " + book.getStatus());
        
        System.out.println();
    }

    private static void demoReservation() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 4: Reservation System");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        // First, checkout a book to make it unavailable
        String isbn = "978-0-13-468599-1"; // Clean Code
        String borrowerId = "P002";
        
        System.out.println("► Patron " + borrowerId + " checks out 'Clean Code'");
        try {
            lendingService.checkoutBook(isbn, borrowerId);
            System.out.println("  ✓ Book checked out");
        } catch (Exception e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        
        // Now another patron tries to reserve it
        String reserverId = "P003";
        System.out.println("\n► Patron " + reserverId + " attempts to reserve 'Clean Code':");
        
        try {
            Reservation reservation = reservationService.reserveBook(isbn, reserverId);
            System.out.println("  ✓ Reservation created!");
            System.out.println("    Reservation ID: " + reservation.getReservationId().substring(0, 8) + "...");
            System.out.println("    Queue position: " + reservationService.getQueuePosition(isbn, reserverId));
        } catch (Exception e) {
            System.out.println("  ✗ Reservation failed: " + e.getMessage());
        }
        
        // Return the book and check if reservation is processed
        System.out.println("\n► Patron " + borrowerId + " returns 'Clean Code':");
        try {
            lendingService.returnBook(isbn, borrowerId);
            System.out.println("  ✓ Book returned");
            
            // Check reservations
            List<Reservation> readyReservations = reservationService.getReadyReservations();
            if (!readyReservations.isEmpty()) {
                System.out.println("  📫 Notification sent to patron with ready reservation!");
            }
        } catch (Exception e) {
            System.out.println("  ✗ " + e.getMessage());
        }
        
        // Check patron notifications
        System.out.println("\n► Checking notifications for patron " + reserverId + ":");
        List<String> notifications = patronService.getNotifications(reserverId);
        for (String notification : notifications) {
            System.out.println("    📩 " + notification);
        }
        
        System.out.println();
    }

    private static void demoRecommendations() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 5: Book Recommendations");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        String patronId = "P001";
        Patron patron = patronService.getPatronById(patronId);
        
        System.out.println("► Patron profile: " + patron.getName());
        System.out.println("  Preferences: " + patron.getPreferences());
        
        System.out.println("\n► Personalized recommendations for " + patron.getName() + ":");
        List<Book> recommendations = recommendationService.getRecommendations(patronId, 3);
        if (recommendations.isEmpty()) {
            System.out.println("    No recommendations available at this time.");
        } else {
            for (Book book : recommendations) {
                System.out.println("    ★ " + book.getTitle() + " by " + book.getAuthor() + 
                                   " [" + book.getGenre() + "]");
            }
        }
        
        System.out.println("\n► Science Fiction recommendations:");
        recommendations = recommendationService.getRecommendationsByGenre("Science Fiction", patronId, 3);
        for (Book book : recommendations) {
            System.out.println("    ★ " + book.getTitle() + " by " + book.getAuthor());
        }
        
        System.out.println("\n► Similar books to '1984':");
        List<Book> similarBooks = recommendationService.getSimilarBooks("978-0-06-112008-4", 3);
        for (Book book : similarBooks) {
            System.out.println("    ★ " + book.getTitle() + " by " + book.getAuthor());
        }
        
        System.out.println();
    }

    private static void demoSystemStatistics() {
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.println("DEMO 6: System Statistics");
        System.out.println("─────────────────────────────────────────────────────────────\n");
        
        System.out.println("► Library Inventory Summary:");
        Map<String, Integer> inventorySummary = bookService.getInventorySummary();
        for (Map.Entry<String, Integer> entry : inventorySummary.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println("\n► Patron Statistics:");
        Map<String, Integer> patronSummary = patronService.getPatronSummary();
        for (Map.Entry<String, Integer> entry : patronSummary.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
        }
        
        System.out.println("\n► Lending Statistics:");
        System.out.println("    Active Loans: " + lendingService.getActiveLoanCount());
        System.out.println("    Overdue Loans: " + lendingService.getOverdueLoanCount());
        System.out.println("    Total Loans: " + lendingService.getTotalLoanCount());
        
        System.out.println("\n► Reservation Statistics:");
        System.out.println("    Active Reservations: " + reservationService.getActiveReservationCount());
        System.out.println("    Ready for Pickup: " + reservationService.getReadyReservations().size());
        
        System.out.println("\n► All Books in Library:");
        printBookList(bookService.getAllBooks());
    }

    private static void printBookList(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("    (No books found)");
        } else {
            for (Book book : books) {
                System.out.println("    • " + book.getTitle() + " by " + book.getAuthor() + 
                                   " (" + book.getPublicationYear() + ") [" + book.getStatus() + "]");
            }
        }
        System.out.println();
    }

    private static void runInteractiveMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        while (running) {
            System.out.println("\n═══════════════════════════════════════════════════════════");
            System.out.println("                    INTERACTIVE MENU                        ");
            System.out.println("═══════════════════════════════════════════════════════════");
            System.out.println("1. List all books");
            System.out.println("2. Search books");
            System.out.println("3. List all patrons");
            System.out.println("4. Checkout book");
            System.out.println("5. Return book");
            System.out.println("6. View active loans");
            System.out.println("7. Get recommendations");
            System.out.println("8. View statistics");
            System.out.println("0. Exit");
            System.out.print("\nChoice: ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    System.out.println("\nAll Books:");
                    printBookList(bookService.getAllBooks());
                    break;
                    
                case "2":
                    System.out.print("Enter search term: ");
                    String searchTerm = scanner.nextLine();
                    System.out.println("\nSearch Results:");
                    printBookList(bookService.searchBooks(searchTerm));
                    break;
                    
                case "3":
                    System.out.println("\nAll Patrons:");
                    for (Patron p : patronService.getAllPatrons()) {
                        System.out.println("    • " + p);
                    }
                    break;
                    
                case "4":
                    System.out.print("Enter book ISBN: ");
                    String isbn = scanner.nextLine();
                    System.out.print("Enter patron ID: ");
                    String pId = scanner.nextLine();
                    try {
                        LoanRecord loan = lendingService.checkoutBook(isbn, pId);
                        System.out.println("✓ Checkout successful! Due: " + loan.getDueDate());
                    } catch (Exception e) {
                        System.out.println("✗ Error: " + e.getMessage());
                    }
                    break;
                    
                case "5":
                    System.out.print("Enter book ISBN: ");
                    isbn = scanner.nextLine();
                    System.out.print("Enter patron ID: ");
                    pId = scanner.nextLine();
                    try {
                        lendingService.returnBook(isbn, pId);
                        System.out.println("✓ Return successful!");
                    } catch (Exception e) {
                        System.out.println("✗ Error: " + e.getMessage());
                    }
                    break;
                    
                case "6":
                    System.out.println("\nActive Loans:");
                    for (LoanRecord loan : lendingService.getActiveLoans()) {
                        System.out.println("    • " + loan);
                    }
                    break;
                    
                case "7":
                    System.out.print("Enter patron ID: ");
                    pId = scanner.nextLine();
                    System.out.println("\nRecommendations:");
                    for (Book book : recommendationService.getRecommendations(pId)) {
                        System.out.println("    ★ " + book.getTitle() + " by " + book.getAuthor());
                    }
                    break;
                    
                case "8":
                    demoSystemStatistics();
                    break;
                    
                case "0":
                    running = false;
                    break;
                    
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
        
        scanner.close();
    }
}
