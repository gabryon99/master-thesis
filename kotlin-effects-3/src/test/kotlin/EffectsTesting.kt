import effects.*
import org.junit.jupiter.api.Test

object Next: Effect<String>

object Read: Effect<String>

object Fail: Effect<Unit>

class Print(val msg: String): Effect<Unit>

class EffectsTesting {

    @Test
    fun `No effects`() {

        val answer = handle {
            42
        } with {
            unhandled()
        }

        assert(answer == 42)
    }

    @Test
    fun `Simple Effect`() {

        val result = handle {
            val prefix = perform { Read }
            "$prefix: hello world!"
        } with {
            when (it) {
                is Read -> {
                    resume("[info]")
                }
                else -> unhandled()
            }
        }

        assert(result == "[info]: hello world!")
    }

    @Test
    fun `Abort effectful function`() {
        val result = handle {
            perform(Fail)
            0
        } with {
            when (it) {
                is Fail -> {
                    42
                }
                else -> unhandled()
            }
        }
        assert(result == 42)
    }

    @Test
    fun `MPretnar - Simple Read Effect`() {

        handle {

            val firstName = perform(Read)
            println("Read firstname: $firstName")
            val lastname = perform(Read)
            println("Read lastname: $lastname")

            println("Full Name: $firstName $lastname")

        } with {effect ->
            when (effect) {
                is Read -> {
                    resume("Bob")
                }
                else -> unhandled()
            }
        }

    }

    @Test
    fun `MPretnar - Reverse output`() {

        val abc: EffectfulFunction<Unit> = {
            perform { Print("A") }
            perform { Print("B") }
            perform { Print("C") }
        }

        val reverseHandler: EffectHandlerFunction<Unit> = {
            when (it) {
                is Print -> {
                    resume()
                    println(it.msg)
                }
                else -> unhandled()
            }
        }

        handle(abc) with reverseHandler
    }

    @Test
    fun `The Next effect`() {

        val result = handle {
            val a = perform(Next).toInt()
            println(a)
            a
        } with {
            when (it) {
                is Next -> {
                    resume("42")
                }
                else -> unhandled()
            }
        }

        assert(result == 42)
    }

}