@file:Suppress("UNREACHABLE_CODE", "UNUSED_EXPRESSION")

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.*
import kotlin.system.exitProcess

fun eprintln(msg: String) = System.err.println(msg)

data class StatefulContinuation(val cont: Continuation<Any>, var resumed: Boolean = false)

class AbortHandlingException(val input: Any) : Throwable()

sealed class Effect {
    internal class Yield(val ret: Int) : Effect()
    internal object Abort : Effect()
}

context(EffectHandler)
suspend fun perform(e: Effect): Any {
    return try {
        suspendCoroutine<Any> {

            // Save the current continuation for being resumed later
            this@EffectHandler.storedContinuation = StatefulContinuation(it)

            // Handle the *thrown* effect using the current handler
            val handled = this@EffectHandler.handle(e)
            // println("[info#$id] Handled value: $handled")

            // If the *handle* block has not been resumed, then abort its continuation
            // returning a value provided by the handler.
            if (!this@EffectHandler.storedContinuation?.resumed!!) {
                this@EffectHandler.abort(handled)
            }
        }
    } catch (ex: AbortHandlingException) {
        throw ex
    }
}

class EffectScope(val handleBlock: suspend context(EffectHandler) () -> Any) {

    companion object {
        private val ID_BUILDER: AtomicInteger = AtomicInteger(-1)
        private val HANDLERS: MutableMap<Int, EffectHandler> = mutableMapOf()
    }

    suspend infix fun with(handleLambda: EffectHandler.(Effect) -> Any): Any {

        val handler = object : EffectHandler {
            override val id: Int = ID_BUILDER.addAndGet(1)
            override var parentHandler: EffectHandler? = HANDLERS[id - 1]
            override var storedContinuation: StatefulContinuation? = null
            override fun handle(e: Effect): Any = handleLambda(this, e)
        }

        // println("[info#${handler.id}] :: Executing handle block...")
        HANDLERS[handler.id] = handler

        return try {
            // Executes the handle block using the build handler
            handleBlock(handler)
        } catch (ex: AbortHandlingException) {
            // If a handler aborted the execution, return its *returned* value
            ex.input
        }
    }
}

interface EffectHandler {

    val id: Int
    var parentHandler: EffectHandler?

    var storedContinuation: StatefulContinuation?

    fun handle(e: Effect): Any

    fun abort(input: Any) {
        storedContinuation?.cont?.resumeWithException(AbortHandlingException(input))
    }

    fun forward(e: Effect) {

        if (parentHandler == null) {
            eprintln("[error#$id] Can't invoke parent handler, since the top scope has been reached.")
            exitProcess(-1)
        }

        // Forward the effect's handling to my parent.
        parentHandler?.storedContinuation = storedContinuation
        parentHandler?.handle(e)

        // Cleanup
        parentHandler?.storedContinuation = null
    }

    fun resume(input: Any) {
        storedContinuation?.resumed = true
        // The resume function mark the coroutine as CoroutineSingletons::RESUMED
        storedContinuation?.cont?.resume(input)
    }
}

fun handle(f: suspend context(EffectHandler) () -> Any): EffectScope = EffectScope(f)

suspend fun main(args: Array<String>) {

    /*
    val outputValue = handle {
        return@handle handle {

            return@handle handle {

                val succ = perform(Effect.Yield(42)) as Int
                perform(Effect.Abort)

                return@handle succ

            } with {
                return@with when (it) {
                    is Effect.Yield -> {
                        resume(it.ret + 1) // Increase the yielded value
                    }
                    else -> {
                        // Interrupt effect handling and return to the handle call-site.
                        10
                    }
                }
            }

        } with {
            return@with when(it) {
                else -> {
                    forward(it)
                }
            }
        }

    } with {
        return@with when(it) {
            else -> {
                resume(Unit)
            }
        }
    }
     */

    val outputValue = handle {

        val succ = perform(Effect.Yield(42)) as Int
        perform(Effect.Abort)

        return@handle succ

    } with {
        return@with when (it) {
            is Effect.Yield -> {
                resume(it.ret + 1) // Increase the yielded value
            }
            else -> {
                // Interrupt effect handling and return to the handle call-site.
                10
            }
        }
    }

    println("Output Value is: $outputValue")

}