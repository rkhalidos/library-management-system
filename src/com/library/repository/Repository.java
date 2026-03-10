package com.library.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface for data access operations.
 * Follows the Repository pattern for abstracted data access.
 *
 * @param <T>  Entity type
 * @param <ID> Entity identifier type
 */
public interface Repository<T, ID> {
    /**
     * Saves an entity to the repository.
     *
     * @param entity Entity to save
     * @return The saved entity
     */
    T save(T entity);

    /**
     * Finds an entity by its ID.
     *
     * @param id Entity ID
     * @return Optional containing the entity if found
     */
    Optional<T> findById(ID id);

    /**
     * Gets all entities in the repository.
     *
     * @return List of all entities
     */
    List<T> findAll();

    /**
     * Deletes an entity by its ID.
     *
     * @param id Entity ID
     * @return true if entity was deleted
     */
    boolean deleteById(ID id);

    /**
     * Checks if an entity exists by its ID.
     *
     * @param id Entity ID
     * @return true if entity exists
     */
    boolean existsById(ID id);

    /**
     * Gets the count of entities in the repository.
     *
     * @return Number of entities
     */
    int count();

    /**
     * Deletes all entities from the repository.
     */
    void deleteAll();
}
