package com.library.factory;

import com.library.model.Patron;
import com.library.model.PatronType;
import com.library.util.LibraryLogger;

import java.util.UUID;

/**
 * Factory class for creating Patron objects.
 * Implements the Factory design pattern for standardized patron creation.
 */
public class PatronFactory {

    /**
     * Creates a patron with a specific type.
     *
     * @param name       Patron's name
     * @param email      Patron's email
     * @param patronType Type of patron
     * @return A new Patron instance
     */
    public static Patron createPatron(String name, String email, PatronType patronType) {
        validatePatronData(name, email);
        String patronId = generatePatronId(patronType);
        Patron patron = new Patron(patronId, name, email, patronType);
        LibraryLogger.logPatronEvent("CREATE", patronId, 
            String.format("%s (%s) - %s", name, patronType, email));
        return patron;
    }

    /**
     * Creates a patron with a custom ID.
     *
     * @param patronId   Custom patron ID
     * @param name       Patron's name
     * @param email      Patron's email
     * @param patronType Type of patron
     * @return A new Patron instance
     */
    public static Patron createPatronWithId(String patronId, String name, String email, PatronType patronType) {
        validatePatronData(name, email);
        if (patronId == null || patronId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patron ID cannot be null or empty");
        }
        Patron patron = new Patron(patronId, name, email, patronType);
        LibraryLogger.logPatronEvent("CREATE", patronId, 
            String.format("%s (%s) - %s", name, patronType, email));
        return patron;
    }

    /**
     * Creates a regular patron.
     *
     * @param name  Patron's name
     * @param email Patron's email
     * @return A new regular Patron instance
     */
    public static Patron createRegularPatron(String name, String email) {
        return createPatron(name, email, PatronType.REGULAR);
    }

    /**
     * Creates a student patron.
     *
     * @param name  Patron's name
     * @param email Patron's email
     * @return A new student Patron instance
     */
    public static Patron createStudentPatron(String name, String email) {
        return createPatron(name, email, PatronType.STUDENT);
    }

    /**
     * Creates a faculty patron.
     *
     * @param name  Patron's name
     * @param email Patron's email
     * @return A new faculty Patron instance
     */
    public static Patron createFacultyPatron(String name, String email) {
        return createPatron(name, email, PatronType.FACULTY);
    }

    /**
     * Creates a senior patron.
     *
     * @param name  Patron's name
     * @param email Patron's email
     * @return A new senior Patron instance
     */
    public static Patron createSeniorPatron(String name, String email) {
        return createPatron(name, email, PatronType.SENIOR);
    }

    /**
     * Creates a patron with preferences already set.
     *
     * @param name        Patron's name
     * @param email       Patron's email
     * @param patronType  Type of patron
     * @param preferences Array of preferences (genres, authors)
     * @return A new Patron instance with preferences
     */
    public static Patron createPatronWithPreferences(String name, String email, 
                                                     PatronType patronType, String... preferences) {
        Patron patron = createPatron(name, email, patronType);
        for (String preference : preferences) {
            patron.addPreference(preference);
        }
        return patron;
    }

    /**
     * Generates a unique patron ID based on patron type.
     *
     * @param patronType Type of patron
     * @return A unique patron ID
     */
    private static String generatePatronId(PatronType patronType) {
        String prefix;
        switch (patronType) {
            case STUDENT:
                prefix = "STU";
                break;
            case FACULTY:
                prefix = "FAC";
                break;
            case SENIOR:
                prefix = "SEN";
                break;
            default:
                prefix = "REG";
        }
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Validates patron data before creation.
     *
     * @param name  Name to validate
     * @param email Email to validate
     * @throws IllegalArgumentException if validation fails
     */
    private static void validatePatronData(String name, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }

    /**
     * Validates email format.
     *
     * @param email Email to validate
     * @return true if email format is valid
     */
    private static boolean isValidEmail(String email) {
        // Simple email validation
        return email.contains("@") && email.contains(".") && 
               email.indexOf("@") < email.lastIndexOf(".");
    }
}
