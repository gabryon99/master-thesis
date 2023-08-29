package effects

import kotlin.coroutines.Continuation

interface EffectHandler<R> : Continuation<R?> {


    /**
     * Resume the execution of an effectful function
     * where it was suspended before performing an effect.
     *
     * Notice that `resume()` can be called only once within the `with` block,
     * since Kotlin does not support multi-shot continuations. However, this restriction
     * doesn't apply when the `resume()` function is used after managing different effects.
     *
     * ```kotlin
     * handle<Unit> {
     *      perform(Yield(42)) // <- Pass execution to the `with` block.
     *      // Continue from here after invoking `resume(...)`
     * } with {
     *      resume()
     * }
     */
    suspend fun <T> resume(input: T): R?

    suspend fun resume(): R? = resume(Unit)

    /**
     * Forward the current effect to the uppermost
     * effect handler.
     *
     * ```kotlin
     * handle {
     *      handle {
     *          perform(Print("A"))
     *      }
     *      with {
     *          forward()
     *      }
     * } with { effect ->
     *      when (effect) {
     *          is Print -> {
     *              println(it.msg)
     *              resume()
     *          }
     *      }
     * }
     * ```
     */
    suspend fun forward(): R? = TODO("To be implemented yet.")

}
