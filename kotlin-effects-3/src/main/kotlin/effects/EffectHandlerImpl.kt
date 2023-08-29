package effects

import effects.exceptions.AlreadyResumedException
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
        RESUMED,
        DONE;
    }

    override val context: CoroutineContext
        get() = CoroutineName("EffectHandler$${this.hashCode()}")

    private var handlerContinuation: Continuation<R?>? = null

    private var result: Result<R?>? = null

    private var status = EffectHandlerStatus.INITIAL

    private var didResume: Boolean = false

    override fun invokeHandler(effect: Effect<*>) {
        status = EffectHandlerStatus.HANDLING

        val effLambda: suspend () -> R? = {
            effectHandlerFunction.invoke(this, effect)
        }

        val effectHandlerCoroutine = effLambda.createCoroutine(this)
        effectHandlerCoroutine.resume(Unit)
    }

    override suspend fun <T> resume(input: T): R? {

        if (status == EffectHandlerStatus.RESUMED) {
            throw AlreadyResumedException("Continuations are linear, therefore you can resume them at least once.")
        }

        status = EffectHandlerStatus.RESUMED
        didResume = true

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
        this.status = EffectHandlerStatus.DONE

        // The effect handler computation ended without resumption.
        // We abort the execution of the effectful function.
        if (didResume) {
            effectfulScope.dismissEffectHandler(this)
        } else {
            effectfulScope.abortComputation(result)
        }
    }

    internal fun unwrapResult(): Result<R?> {
        assert(status == EffectHandlerStatus.DONE)
        return result!!
    }
}