package server.circlehelp.services

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import server.circlehelp.annotations.RepeatableReadTransaction

@Service
@RepeatableReadTransaction
class CallerService {

    fun <T> call(func: () -> T) : T = func()

    fun <T, R> callProxy(proxy: T, func: T.() -> R) = proxy.func()
}