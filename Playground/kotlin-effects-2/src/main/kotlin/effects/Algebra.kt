package effects

typealias EffectfulFunction<R> = suspend EffectHandler<R>.() -> R?
typealias FunEffectHandler<R> = (EffectHandler<R>).(Effect<*>) -> R?

class UnhandledEffectException : Throwable() {}

/**
 * Special marker used to alert a not handled effect.
 */
fun <R> unhandled(): R = throw UnhandledEffectException()

/***
 * Given two handlers h1 and h2, their respective sum `h1 + h2` is a new handler,
 * able to handle the effects of h1 and h2. Note that the sum is not commutative,
 * if both h1 and h2 handle the same effect, h1 will have the priority.
 */
operator fun <R> FunEffectHandler<R>.plus(h2: FunEffectHandler<R>): FunEffectHandler<R> =
    composeHandlers(this, h2)

private fun <R> composeHandlers(h1: FunEffectHandler<R>, h2: FunEffectHandler<R>): FunEffectHandler<R> {
    return h@{
        try {
            return@h h1(this, it)
        } catch (e: UnhandledEffectException) {
            return@h h2(this, it)
        }
    }
}