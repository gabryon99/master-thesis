import effects.EffectHandler
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private const val DEBUG: Boolean = false

/***
 * Print a message on the standard error, in a Kotlin
 * idiomatic way.
 */
fun eprintln(msg: String) = System.err.println(msg)

fun debug(msg: String, prefix: String = "") {
    if (!DEBUG) return
    val format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
    val date = LocalDateTime.now().format(format)
    System.err.println("$date [${Thread.currentThread().name}]$prefix DEBUG :: $msg")
}

context(EffectHandler<*>)
suspend fun debugEff(msg: String) {
    debug(msg, "[Context: '${coroutineContext[CoroutineName.Key]}']")
}

data class CoroutineName(val name: String) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<CoroutineName>

    override fun toString(): String = name
}

class UnreachableCodeException(msg: String) : Throwable(msg)

fun unreachable(msg: String = ""): Nothing = throw UnreachableCodeException(msg)

val <T> Continuation<T>.name get() = this.context[CoroutineName.Key] ?: "Unnamed"