package effects

import kotlin.coroutines.*

class EffectHandlerImpl<R>(
    private val effectfulScope: EffectfulScope<R>,
    private val effectfulFunContinuation: Continuation<Any?>,
    private val effectHandlerFunction: EffectHandlerFunction<R>
): EffectHandler<R> {

    enum class EffectHandlerStatus {
        INITIAL,
        HANDLING,
        SUSPENDED,
        DONE;
    }

    override val context: CoroutineContext
        get() = CoroutineName("EffectHandler$${this.hashCode()}")

    private var handlerContinuation: Continuation<R?>? = null

    private var result: Result<R?>? = null

    private var status = EffectHandlerStatus.INITIAL

    override fun invokeHandler(effect: Effect<*>) {
        status = EffectHandlerStatus.HANDLING

        val effLambda: suspend () -> R? = {
            effectHandlerFunction.invoke(this, effect)
        }

        val effectHandlerCoroutine = effLambda.createCoroutine(this)
        effectHandlerCoroutine.resume(Unit)
    }

    override suspend fun <T> resume(input: T): R? {
        // Resume the effectful function computation
        // where it was stopped.
        effectfulFunContinuation.resume(input)

        return when (effectfulScope.status) {
            EffectfulScope.EffectfulFunctionStatus.COMPUTED -> effectfulScope.unwrapResult().getOrNull()
            EffectfulScope.EffectfulFunctionStatus.PERFORMED_EFFECT -> suspendCoroutine {
                status = EffectHandlerStatus.SUSPENDED
                handlerContinuation = it
            }
            else -> TODO("Unreachable?")
        }
    }

    fun continueEffectHandlerExecution(value: R?) {
        assert(status == EffectHandlerStatus.SUSPENDED)
        assert(handlerContinuation != null)

        status = EffectHandlerStatus.HANDLING
        val handlerCont = handlerContinuation!!
        handlerCont.resume(value)
    }

    override fun resumeWith(result: Result<R?>) {
        this.result = result

        // The effect handler computation ended without resumption.
        // We abort the execution of the effectful function.
        if (this.status == EffectHandlerStatus.HANDLING) {
            this.status = EffectHandlerStatus.DONE
            effectfulScope.status = EffectfulScope.EffectfulFunctionStatus.ABORTED
        }
        else {
            this.status = EffectHandlerStatus.DONE
            effectfulScope.dismissEffectHandler(this)
        }
    }

    internal fun unwrapResult(): Result<R?> {
        assert(status == EffectHandlerStatus.DONE)
        return result!!
    }
}