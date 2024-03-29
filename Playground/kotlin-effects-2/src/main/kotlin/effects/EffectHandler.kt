package effects

import CoroutineName
import debug
import name
import unreachable
import kotlin.coroutines.*

class AlreadyResumedException(message: String) : Throwable(message)
class ForwardException(message: String) : Throwable(message)
enum class EffectHandlerStatus {

    INITIAL,
    HANDLE_SUSPENDED,
    COMPLETED,
    RESUMED;

    override fun toString(): String = when (this) {
        INITIAL -> "Not resumed"
        RESUMED -> "Resumed"
        HANDLE_SUSPENDED -> "Handling an effect"
        COMPLETED -> "Completed"
    }
}

class EffectHandler<R>(
    /**
     * Function defining the handler behavior.
     */
    private val funEffectHandler: FunEffectHandler<R>,
    private var parentHandler: EffectHandler<R>? = null
) : Continuation<R?> {

    override val context: CoroutineContext
        get() = EmptyCoroutineContext + CoroutineName("Effectful Function")

    private var status: EffectHandlerStatus = EffectHandlerStatus.INITIAL

    /**
     * When performing an effect, this will pause the execution of handle's block.
     * Therefore, we have to invoke the handler with the specific effect.
     */
    private var effectToHandle: Effect<*>? = null

    /**
     * This field will contain the result of effectful function.
     */
    private var result: Result<R?>? = null

    private var storedContinuation: Continuation<Any?>? = null


    /**
     * Resume the execution of a handle block, after the call-site
     * of a performed event.
     *
     * Notice that `resume()` can be called only once within the `with` block,
     * since Kotlin does not support multi-shot continuations. However, this restriction
     * doesn't apply when the `resume()` function is used after managing different effects.
     *
     * ```kotlin
     * handle<Unit> {
     *      perform(Effect.Yield(42)) // <- Pass execution to the `with` block.
     *      // Continue from here after invoking `resume(...)`
     * } with {
     *      resume()
     * }
     * ```
     */
    fun <T> resume(input: T): R? {

        // We resumed the continuation, therefore let us change the
        // current status of the effect handler.
        status = EffectHandlerStatus.RESUMED

        val cont = storedContinuation!!
        debug("Resuming coroutine: `${cont.name}`")

        try {
            cont.resume(input)
        } catch (ex: IllegalStateException) {
            throw AlreadyResumedException("The current continuation has been already resumed once.")
        }

        return when (status) {
            EffectHandlerStatus.COMPLETED -> this.result!!.getOrNull()
            EffectHandlerStatus.HANDLE_SUSPENDED -> funEffectHandler.invoke(this, effectToHandle!!)
            else -> unreachable("Should be unreachable")
        }
    }

    /**
     * Forward the given effect to the uppermost effect handler.
     */
    fun forward(e: Effect<*>): R? {
        if (parentHandler == null) {
            throw ForwardException("Cannot invoke parent handler, since the top scope has been reached.")
        }

        val parentHandler = parentHandler!!

        parentHandler.storedContinuation = storedContinuation
        val res = parentHandler.funEffectHandler.invoke(parentHandler, e)
        parentHandler.storedContinuation = null
        return res
    }

    /**
     * Perform the given effect as parameter, and transfer
     * the control to the nearest handler.
     */
    suspend fun <R> perform(effect: Effect<R>): R = suspendCoroutine {
        status = EffectHandlerStatus.HANDLE_SUSPENDED
        effectToHandle = effect
        storedContinuation = it as Continuation<Any?>
    }

    /***
     * This method is called at the end of computing the effectful
     * function. The result field will contain the returned value
     * from the effectful function.
     */
    override fun resumeWith(result: Result<R?>) {
        this.status = EffectHandlerStatus.COMPLETED
        this.result = result
    }

    fun invokeEffectfulFunction(effectfulFun: EffectfulFunction<R>): R? {

        debug("Starting main coroutine for effectful function.")

        while (true) {

            debug("Current state: $status")

            when (status) {
                EffectHandlerStatus.INITIAL -> {
                    // Create and the start the execution of a coroutine
                    val effectfulFunCoroutine = effectfulFun.createCoroutine(this, this)
                    effectfulFunCoroutine.resume(Unit)
                }

                EffectHandlerStatus.HANDLE_SUSPENDED -> {
                    assert(effectToHandle != null)
                    // Someone performed an effect and it has to be handled
                    return funEffectHandler.invoke(this, effectToHandle!!)
                }

                EffectHandlerStatus.COMPLETED -> {
                    // The effectful function completed its cycle, the result
                    // has been stored.
                    assert(result != null)
                    return result!!.getOrNull()
                }

                else -> TODO()
            }
        }

        // unreachable("This point should not be reachable")
    }

}