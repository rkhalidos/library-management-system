# Library Management System

A comprehensive Library Management System implemented in Java, demonstrating Object-Oriented Programming (OOP) principles, SOLID design principles, and various design patterns.

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [SOLID Principles](#solid-principles)
- [Project Structure](#project-structure)
- [Class Diagram](#class-diagram)
- [How to Run](#how-to-run)
- [Usage Examples](#usage-examples)
- [Technical Requirements](#technical-requirements)

## ✨ Features

### Core Features
- **Book Management**: Add, remove, update, and search books by title, author, ISBN, or genre
- **Patron Management**: Register patrons, manage their information, and track borrowing history
- **Lending Process**: Checkout and return books with automatic due date calculation
- **Inventory Management**: Track available and borrowed books in real-time

### Optional Extensions (Implemented)
- **Multi-branch Support**: Support for multiple library branches with book transfers
- **Reservation System**: Allow patrons to reserve books that are currently checked out
- **Notification System**: Automatic notifications when reserved books become available
- **Recommendation System**: Personalized book recommendations based on borrowing history and preferences

## 🏗️ Architecture

The system follows a layered architecture:

```
┌─────────────────────────────────────────────────────┐
│                   Main Application                   │
├─────────────────────────────────────────────────────┤
│                    Service Layer                     │
│  (BookService, PatronService, LendingService, etc.) │
├─────────────────────────────────────────────────────┤
│                  Repository Layer                    │
│  (BookRepository, PatronRepository, LoanRepository) │
├─────────────────────────────────────────────────────┤
│                    Model Layer                       │
│      (Book, Patron, LoanRecord, Reservation)        │
└─────────────────────────────────────────────────────┘
```

## 🎨 Design Patterns

### 1. Observer Pattern
- **Purpose**: Notification system for reserved books
- **Implementation**:
  - `LibraryObserver` interface defines the `update()` method
  - `LibrarySubject` interface defines attach/detach/notify methods
  - `NotificationService` manages subscriptions and notifications
  - `Patron` implements `LibraryObserver` to receive notifications

### 2. Factory Pattern
- **Purpose**: Standardized object creation
- **Implementation**:
  - `BookFactory` creates different types of books (Fiction, Science Fiction, Technical, etc.)
  - `PatronFactory` creates different patron types (Regular, Student, Faculty, Senior)

### 3. Strategy Pattern
- **Purpose**: Flexible search algorithms
- **Implementation**:
  - `SearchStrategy` interface defines the search contract
  - `TitleSearchStrategy` searches by title
  - `AuthorSearchStrategy` searches by author
  - `ISBNSearchStrategy` searches by ISBN
  - `GenreSearchStrategy` searches by genre
  - `CompositeSearchStrategy` combines multiple strategies

### 4. Repository Pattern
- **Purpose**: Abstract data access layer
- **Implementation**:
  - `Repository<T, ID>` generic interface
  - `BookRepository`, `PatronRepository`, `LoanRepository` implementations

## 📐 SOLID Principles

### Single Responsibility Principle (SRP)
Each class has a single, well-defined purpose:
- `Book` handles book data
- `BookService` handles book business logic
- `BookRepository` handles book data access
- `BookFactory` handles book creation

### Open/Closed Principle (OCP)
- New search strategies can be added without modifying existing code
- New patron types can be added through `PatronType` enum

### Liskov Substitution Principle (LSP)
- All repository implementations can be substituted for the `Repository` interface
- All search strategies can be substituted for `SearchStrategy` interface

### Interface Segregation Principle (ISP)
- Small, focused interfaces (`LibraryObserver`, `SearchStrategy`, `Repository`)
- No client is forced to depend on methods it doesn't use

### Dependency Inversion Principle (DIP)
- Services depend on repository interfaces, not concrete implementations
- High-level modules don't depend on low-level modules

## 📁 Project Structure

```
src/
├── Main.java                              # Application entry point
└── com/library/
    ├── model/                             # Domain entities
    │   ├── Book.java
    │   ├── BookStatus.java
    │   ├── Patron.java
    │   ├── PatronType.java
    │   ├── LoanRecord.java
    │   ├── LoanStatus.java
    │   ├── Reservation.java
    │   └── Branch.java
    ├── service/                           # Business logic
    │   ├── BookService.java
    │   ├── PatronService.java
    │   ├── LendingService.java
    │   ├── ReservationService.java
    │   └── RecommendationService.java
    ├── repository/                        # Data access layer
    │   ├── Repository.java
    │   ├── BookRepository.java
    │   ├── PatronRepository.java
    │   └── LoanRepository.java
    ├── observer/                          # Observer pattern
    │   ├── LibraryObserver.java
    │   ├── LibrarySubject.java
    │   └── NotificationService.java
    ├── factory/                           # Factory pattern
    │   ├── BookFactory.java
    │   └── PatronFactory.java
    ├── strategy/                          # Strategy pattern
    │   ├── SearchStrategy.java
    │   ├── TitleSearchStrategy.java
    │   ├── AuthorSearchStrategy.java
    │   ├── ISBNSearchStrategy.java
    │   ├── GenreSearchStrategy.java
    │   └── CompositeSearchStrategy.java
    ├── exception/                         # Custom exceptions
    │   ├── LibraryException.java
    │   ├── BookNotFoundException.java
    │   ├── PatronNotFoundException.java
    │   └── BookNotAvailableException.java
    └── util/                              # Utilities
        └── LibraryLogger.java
```

## 📊 Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              LIBRARY MANAGEMENT SYSTEM                           │
└─────────────────────────────────────────────────────────────────────────────────┘

                                    ┌──────────────────┐
                                    │      Main        │
                                    └────────┬─────────┘
                                             │ uses
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
        ┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
        │   BookService     │    │  PatronService    │    │  LendingService   │
        ├───────────────────┤    ├───────────────────┤    ├───────────────────┤
        │ +addBook()        │    │ +registerPatron() │    │ +checkoutBook()   │
        │ +removeBook()     │    │ +updatePatron()   │    │ +returnBook()     │
        │ +searchBooks()    │    │ +getPatronById()  │    │ +getActiveLoans() │
        │ +getAllBooks()    │    │ +getBorrowHist()  │    │ +getOverdueLoans()│
        └─────────┬─────────┘    └─────────┬─────────┘    └─────────┬─────────┘
                  │                        │                        │
                  │ uses                   │ uses                   │ uses
                  ▼                        ▼                        ▼
        ┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
        │  BookRepository   │    │ PatronRepository  │    │  LoanRepository   │
        ├───────────────────┤    ├───────────────────┤    ├───────────────────┤
        │ +save()           │    │ +save()           │    │ +save()           │
        │ +findById()       │    │ +findById()       │    │ +findById()       │
        │ +findAll()        │    │ +findByEmail()    │    │ +findByPatronId() │
        │ +deleteById()     │    │ +findByType()     │    │ +findActiveLoans()│
        └─────────┬─────────┘    └─────────┬─────────┘    └─────────┬─────────┘
                  │                        │                        │
                  │ stores                 │ stores                 │ stores
                  ▼                        ▼                        ▼
        ┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
        │      Book         │    │      Patron       │    │    LoanRecord     │
        ├───────────────────┤    ├───────────────────┤    ├───────────────────┤
        │ -isbn: String     │    │ -patronId: String │    │ -loanId: String   │
        │ -title: String    │    │ -name: String     │    │ -bookIsbn: String │
        │ -author: String   │    │ -email: String    │    │ -patronId: String │
        │ -pubYear: int     │    │ -patronType: Enum │    │ -checkoutDate     │
        │ -genre: String    │    │ -preferences: Set │    │ -dueDate          │
        │ -status: Enum     │    │ -notifications    │    │ -status: Enum     │
        └───────────────────┘    └─────────┬─────────┘    └───────────────────┘
                                           │ implements
                                           ▼
                                 ┌───────────────────┐
                                 │ LibraryObserver   │◄─────────────────┐
                                 ├───────────────────┤                  │
                                 │ +update()         │                  │ notifies
                                 └───────────────────┘                  │
                                                                        │
                                                              ┌─────────┴─────────┐
                                                              │NotificationService│
                                                              ├───────────────────┤
                                                              │ +attach()         │
                                                              │ +detach()         │
                                                              │ +notifyObservers()│
                                                              └───────────────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DESIGN PATTERNS                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

FACTORY PATTERN                           STRATEGY PATTERN
┌───────────────────┐                     ┌───────────────────┐
│   BookFactory     │                     │ «interface»       │
├───────────────────┤                     │  SearchStrategy   │
│ +createBook()     │                     ├───────────────────┤
│ +createSciFiBook()│                     │ +search()         │
│ +createFiction()  │                     │ +getStrategyName()│
│ +createTechnical()│                     └─────────┬─────────┘
└───────────────────┘                               │
                                          ┌─────────┼─────────┐
┌───────────────────┐                     │         │         │
│  PatronFactory    │                     ▼         ▼         ▼
├───────────────────┤           ┌─────────────┐ ┌─────────┐ ┌─────────────┐
│ +createPatron()   │           │TitleSearch  │ │Author   │ │ISBNSearch   │
│ +createStudent()  │           │Strategy     │ │Search   │ │Strategy     │
│ +createFaculty()  │           └─────────────┘ │Strategy │ └─────────────┘
└───────────────────┘                           └─────────┘

┌─────────────────────────────────────────────────────────────────────────────────┐
│                              ENUMERATIONS                                        │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│   BookStatus    │  │   LoanStatus    │  │   PatronType    │  │ReservationStatus│
├─────────────────┤  ├─────────────────┤  ├─────────────────┤  ├─────────────────┤
│ AVAILABLE       │  │ ACTIVE          │  │ REGULAR(5,14)   │  │ PENDING         │
│ BORROWED        │  │ RETURNED        │  │ STUDENT(7,21)   │  │ READY           │
│ RESERVED        │  │ OVERDUE         │  │ FACULTY(10,30)  │  │ FULFILLED       │
│ TRANSFERRED     │  │                 │  │ SENIOR(5,21)    │  │ CANCELLED       │
└─────────────────┘  └─────────────────┘  └─────────────────┘  │ EXPIRED         │
                                                               └─────────────────┘
```

## 🚀 How to Run

### Prerequisites
- Java JDK 8 or higher
- Command line or IDE (IntelliJ IDEA, Eclipse, VS Code)

### Compile and Run

```bash
# Navigate to the project directory
cd DosLibrary

# Compile all Java files
javac -d out src/Main.java src/com/library/**/*.java

# Run the application
java -cp out Main

# Run with interactive mode
java -cp out Main --interactive
```

### Using IDE
1. Open the project in your preferred IDE
2. Run `Main.java`

## 📖 Usage Examples

### Adding a Book
```java
BookService bookService = new BookService(bookRepository);
Book book = bookService.addBook(
    "978-0-06-112008-4",  // ISBN
    "1984",                // Title
    "George Orwell",       // Author
    1949,                  // Publication Year
    "Science Fiction"      // Genre
);
```

### Registering a Patron
```java
PatronService patronService = new PatronService(patronRepository, loanRepository);
Patron patron = patronService.registerPatron(
    "John Doe",            // Name
    "john@email.com",      // Email
    PatronType.STUDENT     // Type
);
```

### Checking Out a Book
```java
LendingService lendingService = new LendingService(bookRepo, patronRepo, loanRepo);
LoanRecord loan = lendingService.checkoutBook(isbn, patronId);
System.out.println("Due date: " + loan.getDueDate());
```

### Searching for Books
```java
// Search by title
List<Book> results = bookService.searchByTitle("1984");

// Search by author
results = bookService.searchByAuthor("Orwell");

// Composite search (all fields)
results = bookService.searchBooks("fiction");
```

### Reserving a Book
```java
ReservationService reservationService = new ReservationService(bookRepo, patronRepo);
Reservation reservation = reservationService.reserveBook(isbn, patronId);
System.out.println("Queue position: " + reservationService.getQueuePosition(isbn, patronId));
```

### Getting Recommendations
```java
RecommendationService recommendationService = new RecommendationService(bookRepo, patronRepo, loanRepo);
List<Book> recommendations = recommendationService.getRecommendations(patronId, 5);
```

## 📋 Technical Requirements

| Requirement | Implementation |
|-------------|----------------|
| OOP Concepts | ✅ Inheritance, Encapsulation, Polymorphism, Abstraction |
| SOLID Principles | ✅ All five principles applied |
| Design Patterns | ✅ Observer, Factory, Strategy, Repository |
| Java Collections | ✅ List, Set, Map, Queue |
| Logging | ✅ java.util.logging with custom LibraryLogger |
| Exception Handling | ✅ Custom exceptions hierarchy |

## 📝 License

This project is created for educational purposes as part of a Java programming assignment.

## 👤 Author

Library Management System - Java OOP Demonstration Project
