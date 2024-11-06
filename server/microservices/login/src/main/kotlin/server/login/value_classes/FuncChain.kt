package server.login.value_classes

@JvmInline
value class FuncChain<In, Out>(val func: (In) -> Out) {

    fun invoke(arg: In) = func(arg)

    inline fun <Out2> append(crossinline func2: (Out) -> Out2) : FuncChain<In, Out2> {
        return FuncChain { func2(func(it)) }
    }

    inline fun <In2> prepend(crossinline func2: (In2) -> In) : FuncChain<In2, Out> {
        return FuncChain { func(func2(it)) }
    }
}
