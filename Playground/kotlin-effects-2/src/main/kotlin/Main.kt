@file:Suppress("UNCHECKED_CAST")

import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.*
import kotlin.system.exitProcess

fun eprintln(msg: String) = System.err.println(msg)

data class StatefulContinuation<in R>(val cont: Continuation<R>, var resumed: Boolean = false)

data class AbortHandlingException(val returned: Any?) : Throwable()

sealed class Effect<R> {
    internal class YieldInt(val yielded: Int): Effect<Int>()
    internal class YieldString(val yielded: String): Effect<String>()
}

interface EffectHandler {

    /**
     * Represents the handler's identification number.
     */
    val handlerId: Long
    var parentHandler: EffectHandler?
    var storedContinuation: StatefulContinuation<Any?>?

    fun handle(effect: Effect<*>): Any?

    fun <T> resume(input: T) {
        storedContinuation?.resumed = true
        storedContinuation?.cont?.resume(input)
    }

    fun forward(e: Effect<*>): Any? {

        if (parentHandler == null) {
            eprintln("[error#$handlerId] Can't invoke parent handler, since the top scope has been reached.")
            exitProcess(-1)
        }

        val storedContinuation = storedContinuation!!

        // Forward the effect's handling to my parent.
        parentHandler?.storedContinuation = storedContinuation
        val handled = parentHandler?.handle(e)
        parentHandler?.storedContinuation = null

        return handled
    }

    /**
     * Perform the given effect as parameter, and transfer
     * the control to the nearest handler.
     */
    suspend fun <R> perform(effect: Effect<R>): R = suspendCoroutine {

        this.storedContinuation = StatefulContinuation(it as Continuation<Any?>, false)

        val storedContinuation = this.storedContinuation!!

        val handled = this.handle(effect)
        // Did we resume the continuation?
        // Note: The continuation can travel across several handlers.
        if (!storedContinuation.resumed) {
            // A value has been returned by the handler. So, abort the execution of handle's block.
            storedContinuation.cont.resumeWithException(AbortHandlingException(handled))
        }
    }

}

class EffectfulScope(private val effectfulFun: suspend EffectHandler.() -> Any): Continuation<Any?> {

    companion object {
        private val HANDLER_ID_BUILDER = AtomicLong(-1)
        private val REGISTERED_HANDLERS: MutableMap<Long, EffectHandler> = mutableMapOf()
    }

    private var result: Any? = null
    override val context: CoroutineContext = EmptyCoroutineContext

    infix fun with(handler: (EffectHandler).(Effect<*>) -> Any?): Any {

        val newHandlerId = HANDLER_ID_BUILDER.addAndGet(1L)
        val effectHandler = object : EffectHandler {

            override val handlerId: Long = newHandlerId
            override var parentHandler: EffectHandler? = REGISTERED_HANDLERS[handlerId - 1]
            override var storedContinuation: StatefulContinuation<Any?>? = null

            override fun handle(effect: Effect<*>): Any? = handler(this, effect)
        }

        // Register handler
        REGISTERED_HANDLERS[newHandlerId] = effectHandler

        val cont = effectfulFun.createCoroutine(effectHandler, this)
        cont.resume(Unit) // Start the effectful function

        REGISTERED_HANDLERS.remove(newHandlerId)

        return result!!
    }

    override fun resumeWith(result: Result<Any?>) {
        try {
            this.result = result.getOrThrow()
        }
        catch (ex: AbortHandlingException) {
            this.result = ex.returned
        }
    }
}

fun handle(effectfulFun: suspend EffectHandler.() -> Any): EffectfulScope = EffectfulScope(effectfulFun)

fun main(args: Array<String>) {

    val handled = handle {
        return@handle handle {

            val yielded = perform(Effect.YieldInt(42))
            println("${yielded} + 12 = ${yielded + 12}")

            val yieldedStr = perform(Effect.YieldString("Boh?"))

            return@handle yieldedStr

        } with {
            when (it) {
                is Effect.YieldInt -> {
                    resume(it.yielded + 1)
                }
                else -> {
                    forward(it)
                }
            }
        }
    } with {
        42
    }

    println(handled)

}