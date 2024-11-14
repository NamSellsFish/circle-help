package server.circlehelp.services

import org.springframework.stereotype.Service
import server.circlehelp.annotations.RepeatableReadTransaction

@Service
@RepeatableReadTransaction
class CallerService {

    fun <T> call(func: () -> T) : T = func()
}