package server.circlehelp.repositories.readonly

import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.ListPagingAndSortingRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.Repository
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.annotations.RepeatableReadTransaction
import java.util.Optional

@NoRepositoryBean
@ManagementRequiredTransaction(readOnly = true)
interface ReadonlyRepository<T, ID> : ListPagingAndSortingRepository<T, ID> {

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
     * Returns the number of entities available.
     *
     * @return the number of entities.
     */
    fun count(): Long

    /**
     * Returns all instances of the attendanceType.
     *
     * @return all entities
     */
    fun findAll(): List<T>

    /**
     * Returns all instances of the attendanceType `T` with the given IDs.
     *
     *
     * If some or all ids are not found, no entities are returned for these IDs.
     *
     *
     * Note that the order of elements in the result is not guaranteed.
     *
     * @param ids must not be null nor contain any null values.
     * @return guaranteed to be not null. The size can be equal or less than the number of given
     * ids.
     * @throws IllegalArgumentException in case the given [ids][Iterable] or one of its items is null.
     */
    fun findAllById(ids: Iterable<ID>): List<T>
}