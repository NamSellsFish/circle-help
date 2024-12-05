package server.circlehelp.delegated_classes

import java.util.stream.Stream
import kotlin.reflect.KClass

data class DependencyNode(val item: KClass<*>, val children: List<DependencyNode> = listOf()) {

    fun streamAll(): Stream<DependencyNode> {
        return Stream.of(this)
            .let { Stream.concat(it, children.stream().flatMap { it.streamAll() }) }
    }
}
