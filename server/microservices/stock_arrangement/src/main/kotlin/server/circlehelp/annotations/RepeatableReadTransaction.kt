package server.circlehelp.annotations

import jakarta.transaction.InvalidTransactionException
import org.springframework.aot.hint.annotation.Reflective
import org.springframework.core.annotation.AliasFor
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(
    AnnotationRetention.RUNTIME
)
@MustBeDocumented
@Transactional
annotation class RepeatableReadTransaction(
    /**
     * Alias for [.transactionManager].
     * @see .transactionManager
     */
    @get:AliasFor(annotation = Transactional::class) val value: String = "",
    /**
     * A *qualifier* value for the specified transaction.
     *
     * May be used to determine the target transaction manager, matching the
     * qualifier value (or the bean name) of a specific
     * [TransactionManager][org.springframework.transaction.TransactionManager]
     * bean definition.
     * @since 4.2
     * @see .value
     *
     * @see org.springframework.transaction.PlatformTransactionManager
     *
     * @see org.springframework.transaction.ReactiveTransactionManager
     */
    @get:AliasFor(annotation = Transactional::class) val transactionManager: String = "",
    /**
     * Defines zero (0) or more transaction labels.
     *
     * Labels may be used to describe a transaction, and they can be evaluated
     * by individual transaction managers. Labels may serve a solely descriptive
     * purpose or map to pre-defined transaction manager-specific options.
     *
     * See the documentation of the actual transaction manager implementation
     * for details on how it evaluates transaction labels.
     * @since 5.3
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.getLabels
     */
    @get:AliasFor(annotation = Transactional::class) val label: Array<String> = [],
    /**
     * The transaction propagation type.
     *
     * Defaults to [Propagation.REQUIRED].
     * @see org.springframework.transaction.interceptor.TransactionAttribute.getPropagationBehavior
     */
    @get:AliasFor(annotation = Transactional::class) val propagation: Propagation = Propagation.REQUIRED,
    /**
     * The transaction isolation level.
     *
     * Defaults to [Isolation.DEFAULT].
     *
     * Exclusively designed for use with [Propagation.REQUIRED] or
     * [Propagation.REQUIRES_NEW] since it only applies to newly started
     * transactions. Consider switching the "validateExistingTransactions" flag to
     * "true" on your transaction manager if you'd like isolation level declarations
     * to get rejected when participating in an existing transaction with a different
     * isolation level.
     * @see org.springframework.transaction.interceptor.TransactionAttribute.getIsolationLevel
     * @see org.springframework.transaction.support.AbstractPlatformTransactionManager.setValidateExistingTransaction
     */
    @get:AliasFor(annotation = Transactional::class) val isolation: Isolation = Isolation.REPEATABLE_READ,
    /**
     * The timeout for this transaction (in seconds).
     *
     * Defaults to the default timeout of the underlying transaction system.
     *
     * Exclusively designed for use with [Propagation.REQUIRED] or
     * [Propagation.REQUIRES_NEW] since it only applies to newly started
     * transactions.
     * @return the timeout in seconds
     * @see org.springframework.transaction.interceptor.TransactionAttribute.getTimeout
     */
    @get:AliasFor(annotation = Transactional::class) val timeout: Int = TransactionDefinition.TIMEOUT_DEFAULT,
    /**
     * The timeout for this transaction (in seconds).
     *
     * Defaults to the default timeout of the underlying transaction system.
     *
     * Exclusively designed for use with [Propagation.REQUIRED] or
     * [Propagation.REQUIRES_NEW] since it only applies to newly started
     * transactions.
     * @return the timeout in seconds as a String value, e.g. a placeholder
     * @since 5.3
     * @see org.springframework.transaction.interceptor.TransactionAttribute.getTimeout
     */
    @get:AliasFor(annotation = Transactional::class) val timeoutString: String = "",
    /**
     * A boolean flag that can be set to `true` if the transaction is
     * effectively read-only, allowing for corresponding optimizations at runtime.
     *
     * Defaults to `false`.
     *
     * This just serves as a hint for the actual transaction subsystem;
     * it will *not necessarily* cause failure of write access attempts.
     * A transaction manager which cannot interpret the read-only hint will
     * *not* throw an exception when asked for a read-only transaction
     * but rather silently ignore the hint.
     * @see org.springframework.transaction.interceptor.TransactionAttribute.isReadOnly
     * @see org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly
     */
    @get:AliasFor(annotation = Transactional::class) val readOnly: Boolean = false,
    /**
     * Defines zero (0) or more exception [types][Class], which must be
     * subclasses of [Throwable], indicating which exception types must cause
     * a transaction rollback.
     *
     * By default, a transaction will be rolled back on [RuntimeException]
     * and [Error] but not on checked exceptions (business exceptions). See
     * [org.springframework.transaction.interceptor.DefaultTransactionAttribute.rollbackOn]
     * for a detailed explanation.
     *
     * This is the preferred way to construct a rollback rule (in contrast to
     * [.rollbackForClassName]), matching the exception type and its subclasses
     * in a type-safe manner. See the [class-level javadocs][Transactional]
     * for further details on rollback rule semantics.
     * @see .rollbackForClassName
     *
     * @see org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.rollbackOn
     */
    @get:AliasFor(annotation = Transactional::class) val rollbackFor: Array<KClass<out Throwable>>
    = [ RuntimeException::class, Exception::class ],
    /**
     * Defines zero (0) or more exception name patterns (for exceptions which must be a
     * subclass of [Throwable]), indicating which exception types must cause
     * a transaction rollback.
     *
     * See the [class-level javadocs][Transactional] for further details
     * on rollback rule semantics, patterns, and warnings regarding possible
     * unintentional matches.
     * @see .rollbackFor
     *
     * @see org.springframework.transaction.interceptor.RollbackRuleAttribute.RollbackRuleAttribute
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.rollbackOn
     */
    @get:AliasFor(annotation = Transactional::class) val rollbackForClassName: Array<String> = [],
    /**
     * Defines zero (0) or more exception [types][Class], which must be
     * subclasses of [Throwable], indicating which exception types must
     * **not** cause a transaction rollback.
     *
     * This is the preferred way to construct a rollback rule (in contrast to
     * [.noRollbackForClassName]), matching the exception type and its subclasses
     * in a type-safe manner. See the [class-level javadocs][Transactional]
     * for further details on rollback rule semantics.
     * @see .noRollbackForClassName
     *
     * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.rollbackOn
     */
    @get:AliasFor(annotation = Transactional::class) val noRollbackFor: Array<KClass<out Throwable>> = [],
    /**
     * Defines zero (0) or more exception name patterns (for exceptions which must be a
     * subclass of [Throwable]) indicating which exception types must **not**
     * cause a transaction rollback.
     *
     * See the [class-level javadocs][Transactional] for further details
     * on rollback rule semantics, patterns, and warnings regarding possible
     * unintentional matches.
     * @see .noRollbackFor
     *
     * @see org.springframework.transaction.interceptor.NoRollbackRuleAttribute.NoRollbackRuleAttribute
     * @see org.springframework.transaction.interceptor.DefaultTransactionAttribute.rollbackOn
     */
    @get:AliasFor(annotation = Transactional::class) val noRollbackForClassName: Array<String> = []
)

