package server.login.repositories.readonly

import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import java.util.Optional

@NoRepositoryBean
interface ReadonlyRepository<T, ID> {

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be null.
     * @return the entity with the given id or Optional#empty() if none found.
     * @throws IllegalArgumentException if id is null.
     */
    fun findById(id: ID): Optional<T>

    /**
     * Returns whether an entity with the given id exists.
     *
     * @param id must not be null.
     * @return true if an entity with the given id exists, false otherwise.
     * @throws IllegalArgumentException if id is null.
     */
    fun existsById(id: ID): Boolean

    /**
     * Returns all instances of the type.
     *
     * @return all entities
     */
    fun findAll(): Iterable<T>

    /**
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    fun count(): Long
}
