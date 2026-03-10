package com.library.model;

import java.util.Objects;

/**
 * Represents a book in the library system.
 * Implements core attributes and behaviors for book management.
 */
public class Book {
    private final String isbn;
    private String title;
    private String author;
    private int publicationYear;
    private String genre;
    private BookStatus status;
    private String branchId;

    /**
     * Constructs a new Book with the specified attributes.
     *
     * @param isbn            Unique ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @param genre           Book genre/category
     */
    public Book(String isbn, String title, String author, int publicationYear, String genre) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.publicationYear = publicationYear;
        this.genre = genre;
        this.status = BookStatus.AVAILABLE;
        this.branchId = "MAIN"; // Default branch
    }

    /**
     * Constructs a new Book with branch assignment.
     *
     * @param isbn            Unique ISBN identifier
     * @param title           Book title
     * @param author          Book author
     * @param publicationYear Year of publication
     * @param genre           Book genre/category
     * @param branchId        Branch where the book is located
     */
    public Book(String isbn, String title, String author, int publicationYear, String genre, String branchId) {
        this(isbn, title, author, publicationYear, genre);
        this.branchId = branchId;
    }

    // Getters
    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getPublicationYear() {
        return publicationYear;
    }

    public String getGenre() {
        return genre;
    }

    public BookStatus getStatus() {
        return status;
    }

    public String getBranchId() {
        return branchId;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setPublicationYear(int publicationYear) {
        this.publicationYear = publicationYear;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    /**
     * Checks if the book is available for borrowing.
     *
     * @return true if the book is available, false otherwise
     */
    public boolean isAvailable() {
        return status == BookStatus.AVAILABLE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return String.format("Book{isbn='%s', title='%s', author='%s', year=%d, genre='%s', status=%s, branch='%s'}",
                isbn, title, author, publicationYear, genre, status, branchId);
    }
}
