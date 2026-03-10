package com.library.model;

import java.util.*;

/**
 * Represents a library branch in a multi-branch library system.
 * Each branch maintains its own inventory of books.
 */
public class Branch {
    private final String branchId;
    private String name;
    private String address;
    private String phone;
    private final Map<String, Book> inventory; // ISBN -> Book
    private boolean isMainBranch;

    /**
     * Constructs a new Branch.
     *
     * @param branchId Unique branch identifier
     * @param name     Branch name
     * @param address  Branch address
     */
    public Branch(String branchId, String name, String address) {
        this.branchId = branchId;
        this.name = name;
        this.address = address;
        this.inventory = new HashMap<>();
        this.isMainBranch = false;
    }

    /**
     * Constructs a new Branch with main branch flag.
     *
     * @param branchId     Unique branch identifier
     * @param name         Branch name
     * @param address      Branch address
     * @param isMainBranch Whether this is the main branch
     */
    public Branch(String branchId, String name, String address, boolean isMainBranch) {
        this(branchId, name, address);
        this.isMainBranch = isMainBranch;
    }

    // Getters
    public String getBranchId() {
        return branchId;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public boolean isMainBranch() {
        return isMainBranch;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setMainBranch(boolean mainBranch) {
        isMainBranch = mainBranch;
    }

    /**
     * Adds a book to the branch inventory.
     *
     * @param book The book to add
     */
    public void addBook(Book book) {
        book.setBranchId(branchId);
        inventory.put(book.getIsbn(), book);
    }

    /**
     * Removes a book from the branch inventory.
     *
     * @param isbn ISBN of the book to remove
     * @return The removed book, or null if not found
     */
    public Book removeBook(String isbn) {
        return inventory.remove(isbn);
    }

    /**
     * Gets a book from the inventory by ISBN.
     *
     * @param isbn ISBN of the book
     * @return The book, or null if not found
     */
    public Book getBook(String isbn) {
        return inventory.get(isbn);
    }

    /**
     * Checks if a book exists in this branch.
     *
     * @param isbn ISBN of the book
     * @return true if the book exists in this branch
     */
    public boolean hasBook(String isbn) {
        return inventory.containsKey(isbn);
    }

    /**
     * Gets all books in the branch inventory.
     *
     * @return Unmodifiable collection of all books
     */
    public Collection<Book> getAllBooks() {
        return Collections.unmodifiableCollection(inventory.values());
    }

    /**
     * Gets all available books in the branch.
     *
     * @return List of available books
     */
    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book book : inventory.values()) {
            if (book.isAvailable()) {
                available.add(book);
            }
        }
        return available;
    }

    /**
     * Gets the total number of books in this branch.
     *
     * @return Total book count
     */
    public int getTotalBooks() {
        return inventory.size();
    }

    /**
     * Gets the number of available books in this branch.
     *
     * @return Available book count
     */
    public int getAvailableBookCount() {
        int count = 0;
        for (Book book : inventory.values()) {
            if (book.isAvailable()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Transfers a book to another branch.
     *
     * @param isbn         ISBN of the book to transfer
     * @param targetBranch The branch to transfer to
     * @return true if transfer was successful
     */
    public boolean transferBookTo(String isbn, Branch targetBranch) {
        Book book = inventory.get(isbn);
        if (book != null && book.isAvailable()) {
            book.setStatus(BookStatus.TRANSFERRED);
            inventory.remove(isbn);
            book.setStatus(BookStatus.AVAILABLE);
            targetBranch.addBook(book);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Branch branch = (Branch) o;
        return Objects.equals(branchId, branch.branchId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(branchId);
    }

    @Override
    public String toString() {
        return String.format("Branch{id='%s', name='%s', address='%s', books=%d, available=%d, isMain=%s}",
                branchId, name, address, getTotalBooks(), getAvailableBookCount(), isMainBranch);
    }
}
