import TestUtilities.printlnEff
import effects.*
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertFailsWith

object TestUtilities {
    val printlnEff = Println()

    fun <R> buildPrintOutputHandler(stream: PrintStream, prefix: String = ""): FunEffectHandler<R> = {
        when (it) {
            is Println -> {
                stream.print(prefix)
                stream.println(it.message)
                resume(Unit)
            }
            else -> unhandled()
        }
    }
}

class EffectsTesting {

    @Test
    fun `Console effect`() {

        val stdOutputHandler = TestUtilities.buildPrintOutputHandler<Unit>(System.out)
        val stdErrorHandler = TestUtilities.buildPrintOutputHandler<Unit>(System.err)
        val stdOutputHappyHandler = TestUtilities.buildPrintOutputHandler<Unit>(System.out, "[🙂] ")

        val message = "Hello Kotlin! 👋"

        handleWith(stdOutputHandler) { printlnEff(message) }

        handleWith(stdErrorHandler) { printlnEff(message) }

        handleWith(stdOutputHappyHandler) { printlnEff(message) }
    }

    @Test
    fun `Abort handling`() {
        val message = "Hello Kotlin! 👋"
        val value = handle<Int> {
            printlnEff(message)
            return@handle null
        } with {
            return@with 42
        }

        assert(value == 42)
    }

    @Test
    fun `Forwarding an effect`() {
        handle h1@ {

            forwardHandle h2@ {
                val value = perform(Yield<Int>())
                println("I've got a: $value")
                return@h2
            }

            return@h1
        } with w1@ {
            resume(10)
            return@w1 Unit
        }
    }

    @Test
    fun `The Yield effect`() {
        var counter = 0
        handle {
            assert(perform(Yield<Int>()) == 1)
            assert(perform(Yield<Int>()) == 2)
        } with {
            when (it) {
                is Yield<*> -> {
                    counter += 1
                    resume(counter)
                }
                else -> unhandled<Unit>()
            }
        }

        assert(counter == 2)
    }

    @Test
    fun `The Fail effect`() {

        // We convert a String to a non-Int to handle
        // the fail effect.

        val result = handle<Int> h1@ {

            val n = ":)".toIntOrNull()
            if (n == null) {
                 perform(Fail<Int>("Cannot convert String to Int"))
            }
            else {
                return@h1 n
            }

        } with {
            when (it) {
                is Fail<*> -> {
                    debug(it.message)
                    return@with 0
                }
                else -> unhandled()
            }
        }

        assert(result == 0)
    }

    @Test
    fun `The Next effect`() {

        // interface Effect<R>
        // class A: Effect<R>

        val result = handle {
            val a = perform(Next).toInt()
            println(a)
            return@handle a
        } with w@ {
            return@w when (it) {
                is Next -> {
                    resume("42")
                }
                else -> unhandled()
            }
        }

        assert(result == 42)
    }

    @Test
    fun `Resumptions are linear`() {
        assertFailsWith<AlreadyResumedException> {
            handle {

                var x = 0
                if (perform(Choice)) {
                    x = 2
                }
                else {
                    Unit
                }

                println(x)

            } with {
                resume(true)
                resume(false)
            }
        }
    }

}