package effects

import java.util.Stack
import kotlin.coroutines.Continuation
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class EffectfulScope<R>(
    private val effectfulFunction: EffectfulFunction<R>
) {

    enum class EffectfulFunctionStatus {
        INITIAL,
        PERFORMED_EFFECT,
        ABORTED,
        COMPUTED;
    }

    internal var status = EffectfulFunctionStatus.INITIAL

    private var effectToBeHandled: Effect<*>? = null

    private val completionWrapper = ContinuationResultWrapper(
        coroutineName = "EffectfulFunction",
        effectfulScope = this
    )

    private var effectfulFunctionContinuation: Continuation<Any?>? = null

    private var effectHandlerStack: Stack<EffectHandler<R>> = Stack()

    suspend fun <T> perform(effect: Effect<T>): T {
        status = EffectfulFunctionStatus.PERFORMED_EFFECT
        effectToBeHandled = effect
        return suspendCoroutine {
            // Suspend the effectful function and invoke the handler.
            effectfulFunctionContinuation = it as Continuation<Any?>
        }
    }

    suspend fun <R> perform(effect: () -> Effect<R>): R = perform(effect())

    private fun invokeEffectHandler(effectHandlerFunction: EffectHandlerFunction<R>) {

        assert(status == EffectfulFunctionStatus.PERFORMED_EFFECT)
        assert(effectToBeHandled != null)
        assert(effectfulFunctionContinuation != null)

        val effect = effectToBeHandled!!
        // Create a new effect handler within a coroutine
        val newEffectHandler = EffectHandlerImpl(
            this,
            effectfulFunctionContinuation!!,
            effectHandlerFunction
        )
        // Add a new effect handler on top of the stack
        effectHandlerStack.push(newEffectHandler)
        // Invoke handler
        newEffectHandler.invokeHandler(effect)
    }

    internal fun dismissEffectHandler(effectHandler: EffectHandler<R>) {
        effectHandlerStack.remove(effectHandler)
    }

    internal fun unwrapResult(): Result<R?> {
        assert(status == EffectfulFunctionStatus.COMPUTED || status == EffectfulFunctionStatus.ABORTED)
        return completionWrapper.result!!
    }

    internal fun abortComputation(result: Result<R?>) {
        this.completionWrapper.result = result
        this.status = EffectfulFunctionStatus.ABORTED
    }

    infix fun with(effectHandler: EffectHandlerFunction<R>): R? {

        var effectfulFunctionCoroutine: Continuation<Unit>

        while (true) {
            when {
                status == EffectfulFunctionStatus.INITIAL -> {
                    effectfulFunctionCoroutine = effectfulFunction.createCoroutine(this, completionWrapper)
                    effectfulFunctionCoroutine.resume(Unit)
                }
                status == EffectfulFunctionStatus.PERFORMED_EFFECT -> {
                    invokeEffectHandler(effectHandler)
                }
                status == EffectfulFunctionStatus.ABORTED -> {
                    return unwrapResult().getOrNull()
                }
                status == EffectfulFunctionStatus.COMPUTED && !effectHandlerStack.empty() -> {
                    // If the effect handler stack is not empty,
                    // it means there are some suspended effect handlers
                    // that are to be resumed.
                    var result = unwrapResult().getOrNull()

                    while (!effectHandlerStack.empty()) {
                        val effHandler = effectHandlerStack.pop() as EffectHandlerImpl
                        effHandler.continueEffectHandlerExecution(result)
                        result = effHandler.unwrapResult().getOrNull()
                    }
                }
                status == EffectfulFunctionStatus.COMPUTED && effectHandlerStack.empty() -> {
                    return unwrapResult().getOrNull()
                }
            }
        }
    }
}
