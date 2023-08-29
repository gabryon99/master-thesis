package effects

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

data class CoroutineName(val name: String) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<CoroutineName>

    override fun toString(): String = name
}

class UnreachableCodeException(msg: String) : Throwable(msg)

fun unreachable(msg: String = ""): Nothing = throw UnreachableCodeException(msg)

val <T> Continuation<T>.name get() = this.context[CoroutineName.Key] ?: "Unnamed"