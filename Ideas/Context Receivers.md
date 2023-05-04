The code shown by the user can be mimicked easily in Kotlin using Context Receivers, as the example below shows.

```kotlin
interface EffectContextReceiver

interface RandomEffect : EffectContextReceiver {
	fun random(): Int
}

class UniformRandomHandler(private val from: Int, private val to: Int): RandomEffect {
	private val rng = Random(System.currentTimeMillis())
	override fun random(): Int = rng.nextInt(from, to)
}

fun main() {
	val randomVector = with(UniformRandomHandler(1, 32)) {
		(0..16).map { random() }
	}
	println(randomVector)
}

```
