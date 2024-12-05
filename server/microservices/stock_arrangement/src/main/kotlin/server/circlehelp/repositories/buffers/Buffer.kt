package server.circlehelp.repositories.buffers

import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Example
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.repository.query.FluentQuery
import server.circlehelp.repositories.TransactionalJpaRepository
import server.circlehelp.repositories.caches.CacheRepository
import server.circlehelp.repositories.readonly.ReadonlyRepository
import java.util.Comparator
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

