package effects

class EffectfulScope<R>(private val effectfulFun: EffectfulFunction<R>) {

    companion object {
        private val REGISTERED_HANDLERS: MutableList<EffectHandler<*>?> = mutableListOf()
    }

    infix fun with(handler: FunEffectHandler<R>): R? {

        val latestHandler = REGISTERED_HANDLERS.lastOrNull() as EffectHandler<R>?
        val effectHandler = EffectHandler(handler, latestHandler)

        REGISTERED_HANDLERS.add(effectHandler)
        val result = effectHandler.invokeEffectfulFunction(effectfulFun)
        REGISTERED_HANDLERS.remove(effectHandler)

        return result
    }

}