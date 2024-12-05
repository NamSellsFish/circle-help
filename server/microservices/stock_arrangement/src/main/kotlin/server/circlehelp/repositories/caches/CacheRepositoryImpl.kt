package server.circlehelp.repositories.caches

import io.reactivex.rxjava3.core.Observable
import jakarta.persistence.EntityManager
import jakarta.persistence.LockModeType
import org.springframework.cache.Cache
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.repositories.readonly.ReadonlyRepository
import server.circlehelp.services.TableWatcherBuilderService
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@ManagementRequiredTransaction(readOnly = true)
class CacheRepositoryImpl<T : Any, ID : Any>(
    private val tableName: String,
    private val keySelector: (T) -> ID,
    private val readonlyRepository: ReadonlyRepository<T, ID>,
    tableWatcherBuilderService: TableWatcherBuilderService,
    private val cacheManager: CacheManager,
    private val entityManager: EntityManager,
    private val otherTableNames: Iterable<String> = listOf()
): CacheRepository<T, ID> {

    var updateEvent: () -> Unit = {}

    var size = readonlyRepository.count()
    var elements = readonlyRepository.findAll()

    private val tableWatcher = tableWatcherBuilderService.fromSnakeCase(
        listOf(tableName).plus(otherTableNames),
        ::update
    )

    override fun checkTables() = tableWatcher.checkTables() != null

    override fun update() {
        elements = readonlyRepository.findAll()
        size = elements.size.toLong()
        sortCache.invalidate()
        pageableCache.invalidate()
        tableCache.invalidate()
        elements.forEach { tableCache.put(keySelector(it), it) }
        updateEvent()
    }

    override fun lock(lock: LockModeType): Observable<Unit> {
        return elements.let { Observable.fromIterable(it) }
            .map { entityManager.lock(it, lock) }
    }

    val tableCache: Cache
        get() = cacheManager.getCache(tableName)!!

    val sortCache: Cache
        get() = cacheManager.getCache("${tableName}.sort")!!

    val pageableCache: Cache
        get() = cacheManager.getCache("${tableName}.pageable")!!

    override fun findById(id: ID): Optional<T> {

        return Optional.ofNullable(tableCache.get(id) {
            readonlyRepository.findById(id).getOrNull()
        })
    }

    override fun existsById(id: ID): Boolean {
        return cacheManager.getCache(tableName)!!.get(id)?.get() != null || readonlyRepository.existsById(id)
    }

    override fun count(): Long = size

    override fun findAll(): List<T> = elements

    override fun findAll(sort: Sort): MutableList<T> {
        return sortCache.get(sort, {readonlyRepository.findAll(sort)})!!.toMutableList()
    }

    override fun findAll(pageable: Pageable): Page<T> {
        return pageableCache.get(pageable, { readonlyRepository.findAll(pageable) })!!
    }

    override fun findAllById(ids: Iterable<ID>): List<T> {
        return ids.mapNotNull { findById(it).getOrNull() }
    }

    companion object {

        fun <T, ID : Any> Cache.getOrPutIfNotNull(obj: ID, func: (ID) -> T?): T? {
            val result = get(obj)

            if (result == null) {
                val newResult = func(obj)
                if (newResult != null)
                    put(obj, newResult)
                return newResult
            }
            else
                return result.get() as T
        }
    }
}