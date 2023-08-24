package effects

interface Effect<R> {
    /***
     * Syntactic sugar to perform an effect
     */
    context(EffectHandler<*>)
    suspend fun perform(): R = this@EffectHandler.perform<R>(this@Effect)
}

context(EffectHandler<*>)
suspend fun <R> perform(e: Effect<R>): R = e.perform()

private fun <R> forwardHandler(): FunEffectHandler<R> = { effect ->
    forward(effect)
}

private fun <R> unhandledHandler(): FunEffectHandler<R> = h@{
    return@h unhandled()
}

fun <R> handle(effectfulFun: EffectfulFunction<R>): EffectfulScope<R> = EffectfulScope(effectfulFun)

fun <R> handleWith(handler: FunEffectHandler<R>, effectfulFun: EffectfulFunction<R>): R? =
    handle(effectfulFun) with handler

fun <R> forwardHandle(effectfulFun: EffectfulFunction<R>): R? = handle(effectfulFun) with forwardHandler()

fun <R> unHandle(effectfulFun: EffectfulFunction<R>): R? = handle(effectfulFun) with unhandledHandler()

// fun <R> implicitHandle(effectfulFun: EffectfulFunction<R>): R? = handle(effectfulFun) with ImplicitHandler