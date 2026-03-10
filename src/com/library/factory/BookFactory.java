package com.library.factory;

import com.library.model.Book;
import com.library.util.LibraryLogger;

/**
 * Factory class for creating Book objects.
 * Implements the Factory design pattern for standardized book creation.
 */
public class BookFactory {

    /**
     * Creates a standard book with all required attributes.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @param genre           Book genre
     * @return A new Book instance
     */
    public static Book createBook(String isbn, String title, String author, int publicationYear, String genre) {
        validateBookData(isbn, title, author, publicationYear);
        Book book = new Book(isbn, title, author, publicationYear, genre);
        LibraryLogger.logBookEvent("CREATE", isbn, String.format("'%s' by %s", title, author));
        return book;
    }

    /**
     * Creates a book assigned to a specific branch.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @param genre           Book genre
     * @param branchId        Branch identifier
     * @return A new Book instance assigned to the branch
     */
    public static Book createBookForBranch(String isbn, String title, String author, 
                                           int publicationYear, String genre, String branchId) {
        validateBookData(isbn, title, author, publicationYear);
        Book book = new Book(isbn, title, author, publicationYear, genre, branchId);
        LibraryLogger.logBookEvent("CREATE", isbn, 
            String.format("'%s' by %s for branch %s", title, author, branchId));
        return book;
    }

    /**
     * Creates a fiction book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new fiction Book instance
     */
    public static Book createFictionBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Fiction");
    }

    /**
     * Creates a non-fiction book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new non-fiction Book instance
     */
    public static Book createNonFictionBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Non-Fiction");
    }

    /**
     * Creates a science fiction book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new science fiction Book instance
     */
    public static Book createSciFiBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Science Fiction");
    }

    /**
     * Creates a mystery book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new mystery Book instance
     */
    public static Book createMysteryBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Mystery");
    }

    /**
     * Creates a technical/programming book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new technical Book instance
     */
    public static Book createTechnicalBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Technical");
    }

    /**
     * Creates a biography book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new biography Book instance
     */
    public static Book createBiography(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Biography");
    }

    /**
     * Creates a children's book.
     *
     * @param isbn            ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @return A new children's Book instance
     */
    public static Book createChildrensBook(String isbn, String title, String author, int publicationYear) {
        return createBook(isbn, title, author, publicationYear, "Children");
    }

    /**
     * Validates book data before creation.
     *
     * @param isbn            ISBN to validate
     * @param title           Title to validate
     * @param author          Author to validate
     * @param publicationYear Year to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validateBookData(String isbn, String title, String author, int publicationYear) {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("ISBN cannot be null or empty");
        }
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (author == null || author.trim().isEmpty()) {
            throw new IllegalArgumentException("Author cannot be null or empty");
        }
        if (publicationYear < 0 || publicationYear > java.time.Year.now().getValue() + 1) {
            throw new IllegalArgumentException("Invalid publication year: " + publicationYear);
        }
    }
}
