package effects

import kotlin.coroutines.Continuation

interface EffectHandler<R> : Continuation<R?> {

    fun invokeHandler(effect: Effect<*>)

    /***
     * Resume the execution of an effectful function
     * where it was suspended before performing an effect.
     */
    suspend fun <T> resume(input: T): R?

    suspend fun resume(): R? = resume(Unit)
}
