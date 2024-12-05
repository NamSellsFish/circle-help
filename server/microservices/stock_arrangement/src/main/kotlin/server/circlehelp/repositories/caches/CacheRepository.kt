package server.circlehelp.repositories.caches

import io.reactivex.rxjava3.core.Observable
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.repository.NoRepositoryBean
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.repositories.readonly.ReadonlyRepository

@NoRepositoryBean
@ManagementRequiredTransaction(readOnly = true)
interface CacheRepository<T, ID>: ReadonlyRepository<T, ID> {

    /**
     * true if the cache updated.
     */
    fun checkTables(): Boolean

    fun update()

    fun lock(lock: LockModeType): Observable<Unit>
}