package effects

typealias EffectfulFunction<R> = suspend (EffectfulScope<R>).() -> R?
typealias EffectHandlerFunction<R> = suspend (EffectHandler<R>).(Effect<*>) -> R?

interface Effect<R>

fun <R> handle(effectfulFunction: EffectfulFunction<R>): EffectfulScope<R> =
    EffectfulScope(effectfulFunction)

fun <R> handleWith(effectHandlerFunction: EffectHandlerFunction<R>, effectfulFunction: EffectfulFunction<R>): R? {
    return handle(effectfulFunction) with effectHandlerFunction
}