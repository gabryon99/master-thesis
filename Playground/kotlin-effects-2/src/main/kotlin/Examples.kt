@file:Suppress("UNCHECKED_CAST")

import java.io.PrintStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class LimitExceeded(): Throwable()

// Effectful functions
val printlnEff = Effect.Println()

val putEff = Effect.KvStorePut<String, String>()
val getEff = Effect.KvStoreGet<String, String>()
val randEff = Effect.RandomInt()
val requestEff = Effect.Http()

val throwEff = Effect.CanThrow<Int>()

// Questions:
// 1. How do I define a new pre-built handler? ✅
// 2. How do I combine effect handlers? Like a union of them? ✅
// 3. Resumption can be forgotten! Is this intentional tough?

val ImplicitHandler: FunEffectHandler = {
    UNHANDLED
}

val ForwardHandler: FunEffectHandler = {
    forward(it)
}

fun buildPrintOutputHandler(stream: PrintStream, prefix: String = ""): FunEffectHandler = {
    when (it) {
        is Effect.Println -> {
            stream.print(prefix)
            stream.println(it.message)
            resume(Unit)
            println("After `resume`")
        }
        else -> UNHANDLED
    }
}

val stdOutputHandler        = buildPrintOutputHandler(System.out)
val stdErrorHandler         = buildPrintOutputHandler(System.err)
val stdOutputHappyHandler   = buildPrintOutputHandler(System.out, "[🙂] ")

fun example0() {
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

fun example1() {

    val message = "Hello Kotlin! 👋"

    handle { printlnEff(message) } with stdOutputHandler

    // This will be printed in red
    handle { printlnEff(message) } with stdErrorHandler

}

fun example_1b() {
    handle {
        printlnEff("Forwarded Effect!")
    } with ForwardHandler
}

fun example2() {

    val storage = mutableMapOf<String, String>()
    val memoryStorageHandler: FunEffectHandler = {
        when (it) {
            is Effect.KvStoreGet<*, *> -> {
                resume(storage[it.key])
            }
            is Effect.KvStorePut<*, *> -> {
                storage[it.key as String] = it.value as String
                resume(Unit)
            }
            else -> {}
        }
    }

    handle {
        // Put a key
        putEff("Gabriele", "gabriele.pappalardo@jetbrains.com")
        // Retrieve the key from the storage
        val value = getEff("Gabriele")
        println(value)
    } with memoryStorageHandler

}

fun example3() {

    val storage = mutableMapOf<String, String>()
    val memoryStorageHandler: FunEffectHandler = {
        when (it) {
            is Effect.KvStoreGet<*, *> -> {
                println("[debug] :: Getting a value given the key: '${it.key}'")
                resume(storage[it.key])
            }
            is Effect.KvStorePut<*, *> -> {
                println("[debug] :: Storing a key-value pair: ('${it.key}', '${it.value}')")
                storage[it.key as String] = it.value as String
                resume(Unit)
            }
            else -> UNHANDLED
        }
    }

    // Composing effect handlers
    // The addition operation is not commutative!

    handle {

        // Put a key
        putEff("Gabriele", "gabriele.pappalardo@jetbrains.com")

        // Retrieve the key from the storage
        val value = getEff("Gabriele")
        printlnEff(value)

    } with (memoryStorageHandler + stdOutputHappyHandler)

}

fun example4() {

    val randomNumberHandler: FunEffectHandler = {
        when (it) {
            is Effect.RandomInt -> {
                resume(it.rng.nextInt() % 10)
            }
            else -> UNHANDLED
        }
    }

    handle {

        // Guessing game
        val secretNumber = randEff()
        var attempts = 3
        var guess = 0

        printlnEff("I am thinking a number try to guess it")

        do {

            printlnEff("Input your number (attempts: $attempts):")
            guess = readln().toInt()

            when {
                guess < secretNumber -> {
                    printlnEff("Too low")
                    attempts -= 1
                }
                guess > secretNumber -> {
                    printlnEff("Too high")
                    attempts -= 1
                }
            }
        } while (guess != secretNumber && attempts != 0)

        if (guess == secretNumber) {
            printlnEff("You guessed: $secretNumber")
        }
        else {
            printlnEff("You lose the game, the secret number was: $secretNumber")
        }
    } with (randomNumberHandler + stdOutputHappyHandler)

}

fun example5() {

    // HTTP request?
    val httpHandler: FunEffectHandler = {
        when (it) {
            is Effect.Http -> {
                try {
                    val client = HttpClient.newBuilder().build();
                    val request = HttpRequest.newBuilder()
                        .uri(URI.create(it.url!!))
                        .build();
                    val response = client.send(request, HttpResponse.BodyHandlers.ofString());
                    resume(response.body())
                } catch (ex: Exception) {
                    resume("{\"name\": \"missigno\"}")
                }
            }
            else -> UNHANDLED
        }
    }

    handle {

        val url = "https://pokeapi.co/api/v2/pokemon/pikachu"
        val jsonData = requestEff(url)
        val pokemon = Json.parseToJsonElement(jsonData).jsonObject

        printlnEff("The fetched Pokémon is: ${pokemon["name"]}")

    } with stdOutputHandler + httpHandler

}

fun example6() {

    val limitHandler: FunEffectHandler = {
        when (it) {
            is Effect.CanThrow -> {
                resume(0.toDouble())
            }
            else -> UNHANDLED
        }
    }

    val limit = 128
    val f: (Double) -> EffectfulScope = { x ->
        handle {
            return@handle if (x < limit) { x * x } else { throwEff(LimitExceeded()) }
        }
    }

    val doubles = listOf<Double>(10.toDouble(), 20.toDouble(), 30.toDouble(), 256.toDouble())

    val res: List<Double> = doubles.map {
        (f(it) with limitHandler) as Double
    }

    println(res)
}

suspend fun foo(): Unit = Unit

context(Int)
fun foo(): Unit = Unit


fun <A, B> List<A>.effMap(f: (A) -> B, handler: FunEffectHandler? = null): List<B> {
    return if (handler == null) {
        this.map(f)
    }
    else {
        (handle {
            val res = mutableListOf<B>()
            for (x in this@effMap) res.add(f(x))
            return@handle res
        } with handler) as List<B>
    }
}

fun example8() = handle {
    printlnEff("Hello World")
} with {
    forward(it)
}

fun example9() {

    val numbers = listOf(1, 2, 3, 4, 5)
    val doubled = numbers.effMap({it * 2})

    println(doubled)

}

fun example10() {

    val limit = 25

    val mapped = handle {
        return@handle listOf(10, 20, 30).map {
            if (it < limit) it * 2 else throwEff(LimitExceeded())
        }
    } with {
        when (it) {
            is Effect.CanThrow -> {
                println("Limit exceeded!")
            }
            else -> UNHANDLED
        }
    }

    println(mapped)

}

/**
 *  map([1,2,3], {it * 2}) [with GlobalForwardHandler]
 *  map([1,2,3], {it * 2}) with customHandler
 */

fun example11_a() {
    handle {
        example11_b()
    } with {
        when (it) {
            is Effect.YieldInt -> {
                println(it.yielded)
                resume(it.yielded)
            }
            else -> UNHANDLED
        }
    }
}

fun example11_b() {
    handle {
        Effect.YieldInt(10).perform()
    } with {
        forward(it)
    }
}

context(EffectHandler)
suspend fun foo12() {
    Effect.YieldInt(10).perform()
}

fun example12() {
    handle {
        foo12()
    } with {
        when (it) {
            else -> UNHANDLED
        }
    }
}


//region Non-determinism

data class Solution(val first: Int, val second: Int, val third: Int)

val flip = Effect.Flip()
fun <A> fail() = Effect.Fail<A>()

fun choice(n: Int): Int = (handle {
    if (n < 1) {
        fail<Int>()
    }
    else if (flip()) {
        n
    }
    else {
        choice(n - 1)
    }
} with ForwardHandler) as Int



//endregion