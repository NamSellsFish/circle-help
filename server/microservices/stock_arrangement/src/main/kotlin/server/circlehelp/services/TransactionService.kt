package server.circlehelp.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import server.circlehelp.annotations.ManagementRequiredTransaction
import server.circlehelp.annotations.RepeatableReadTransaction

/**
 * Provides method to wrap code blocks inside a transaction.
 */
@Service
class TransactionService {


    /**
     * Propagation: [Propagation.REQUIRED]
     *
     * Isolation Level: [Isolation.REPEATABLE_READ]
     *
     * Rollback on: [RuntimeException], [Exception]
     */
    @RepeatableReadTransaction
    fun <T> requiredRollbackOnAny(func: () -> T) : T = func()

    /**
     * Propagation: [Propagation.REQUIRES_NEW]
     *
     * Isolation Level: [Isolation.REPEATABLE_READ]
     *
     * Rollback on: [Throwable]
     */
    @RepeatableReadTransaction(propagation = Propagation.REQUIRES_NEW)
    fun <T> requiresNewRollbackOnAny(func: () -> T) : T = func()

    /**
     * Propagation: [Propagation.MANDATORY]
     *
     * Isolation Level: [Isolation.REPEATABLE_READ]
     *
     * Rollback on: Never
     */
    @ManagementRequiredTransaction
    fun <T> managementRequired(func: () -> T) : T = func()

    @ManagementRequiredTransaction(readOnly = true)
    fun <T> managementRequiredReadonly(func: () -> T) : T = func()

    @RepeatableReadTransaction
    fun <T, R> callProxy(proxy: T, func: T.() -> R) = proxy.func()
}