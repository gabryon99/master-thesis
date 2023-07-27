import effect.Effect
import effect.EffectHandler
import effect.handle

class MapYield<X, Y>: Effect<X, Y>

typealias MapYieldHandler<X, Y> = EffectHandler<X, Y, MapYield<X, Y>>

context(MapYieldHandler<X, Y>)
suspend fun <X, Y> map(inputList: List<X>, f: suspend context(MapYieldHandler<X, Y>) (X) -> Y): List<Y> {
    val outputList = mutableListOf<Y>()
    for (x in inputList) {
        outputList.add(f(this@MapYieldHandler, x))
    }
    return outputList
}

suspend fun main(args: Array<String>) {

    val evenNumbers = listOf(2, 4, 6, 8, 10)
    var outputList = listOf<Int>()

    handle<MapYield<Int, Int>, _, _, _> {
        outputList = map(evenNumbers) { MapYield<Int, Int>().perform(it) }
        return@handle 0
    } with {_, value ->
        resume(value * 2)
    }

    println(outputList)

}