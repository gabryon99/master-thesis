package effect

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

abstract class EffectHandler<A, R, E: Effect<A, R>> {
    var storedContinuation: Continuation<R>? = null

    abstract fun handle(effect: E, data: A): Unit

    fun resume(input: R) = storedContinuation?.resume(input)
}

interface Effect<A, R> {
    context(EH)
    @Suppress("UNCHECKED_CAST")
    suspend fun <E: Effect<A, R>, EH: EffectHandler<A, R, E>> perform(input: A): R = suspendCoroutine {
        // Save the current continuation for later
        storedContinuation = it
        // Handle the following effect
        handle(this@Effect as E, input)
    }
}

class EffectScope<A, R, E: Effect<A, R>, EH : EffectHandler<A, R, E>>(val lambda: suspend context(EH) () -> R) {
    suspend inline infix fun with(handler: EH): R = lambda(handler)

    @Suppress("UNCHECKED_CAST")
    suspend inline infix fun with(crossinline handleLambda: (EH).(E, A) -> Unit): R {
        val handler = object : EffectHandler<A, R, E> () {
            override fun handle(effect: E, data: A) = handleLambda(this as EH, effect, data)
        }
        return lambda(handler as EH)
    }
}

fun <E: Effect<A, R>, EH: EffectHandler<A, R, E>, A, R> handle(lambda: suspend context(EH) () -> R): EffectScope<A, R, E, EH> =
    EffectScope(lambda)
