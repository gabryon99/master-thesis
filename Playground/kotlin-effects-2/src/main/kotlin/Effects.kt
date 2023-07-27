@file:Suppress("UNCHECKED_CAST")

import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.*
import kotlin.random.Random
import kotlin.system.exitProcess

fun eprintln(msg: String) = System.err.println(msg)

object UNHANDLED

data class StatefulContinuation<in R>(val cont: Continuation<R>, var resumed: Boolean = false)

data class AbortHandlingException(val returned: Any?) : Throwable()

sealed class Effect<R> {

    class Flip: Effect<Boolean>() {
        context(EffectHandler)
        suspend operator fun invoke(): Boolean = this@EffectHandler.perform<Boolean>(this)
    }
    class Fail<A>: Effect<A>() {
        context(EffectHandler)
        suspend operator fun invoke(): A = this@EffectHandler.perform<A>(this)
    }

    class YieldInt(val yielded: Int): Effect<Int>()
    class YieldString(val yielded: String): Effect<String>()

    class Println(): Effect<Unit>() {
        var message: String? = null

        context(EffectHandler)
        suspend operator fun invoke(message: String): Unit {
            this.message = message
            return this@EffectHandler.perform<Unit>(this)
        }
    }

    class KvStoreGet<A, B>(): Effect<B>() {
        var key: A? = null
        context(EffectHandler)
        suspend operator fun invoke(key: A): B {
            this.key = key
            return this@EffectHandler.perform<B>(this)
        }
    }
    class KvStorePut<A, B>(): Effect<Unit>() {

        var key: A? = null
        var value: B? = null

        context(EffectHandler)
        suspend operator fun invoke(key: A, value: B): Unit {
            this.key = key
            this.value = value
            return this@EffectHandler.perform<Unit>(this)
        }
    }

    class RandomInt: Effect<Int>() {
        val rng = Random(System.nanoTime())
        context(EffectHandler)
        suspend operator fun invoke(): Int {
            return this@EffectHandler.perform<Int>(this)
        }
    }

    class Http: Effect<String>() {
        var url: String? = null
        context(EffectHandler)
        suspend operator fun invoke(url: String): String {
            this.url = url
            return this@EffectHandler.perform(this)
        }
    }

    class CanThrow<T>: Effect<T>() {
        var exc: Throwable? = null
        context(EffectHandler)
        suspend operator fun invoke(exc: Throwable): T {
            this.exc = exc
            return this@EffectHandler.perform<_>(this)
        }
    }

    /***
     * Syntactic sugar to perform an effect
     */
    context(EffectHandler)
    suspend fun perform(): R = this@EffectHandler.perform<R>(this@Effect)

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
        //println("Resuming?")
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

typealias FunEffectHandler = (EffectHandler).(Effect<*>) -> Any?

operator fun FunEffectHandler.plus(h2: FunEffectHandler): FunEffectHandler = composeHandlers(this, h2)
private fun composeHandlers(h1: FunEffectHandler, h2: FunEffectHandler): FunEffectHandler {
    return {
        val h1Res = h1(this, it)
        if (h1Res === UNHANDLED) {
            h2(this, it)
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

fun handle(effectfulFun: suspend EffectHandler.() -> Any): EffectfulScope =
    EffectfulScope(effectfulFun)

