package effects

import kotlin.random.Random

object Choice : Effect<Boolean>

class Fail<A>(val message: String = "") : Effect<A> {
    context(EffectHandler<*>)
    suspend operator fun invoke(): A = this@EffectHandler.perform<A>(this)
}

object Next : Effect<String>

class Yield<A> : Effect<A>

class Println : Effect<Unit> {
    var message: String? = null

    context(EffectHandler<*>)
    suspend operator fun invoke(message: String): Unit {
        this.message = message
        return this@EffectHandler.perform<Unit>(this)
    }
}

class KvStoreGet<A, B>() : Effect<B> {
    var key: A? = null
    context(EffectHandler<*>)
    suspend operator fun invoke(key: A): B {
        this.key = key
        return this@EffectHandler.perform<B>(this)
    }
}

class KvStorePut<A, B>() : Effect<Unit> {

    var key: A? = null
    var value: B? = null

    context(EffectHandler<*>)
    suspend operator fun invoke(key: A, value: B): Unit {
        this.key = key
        this.value = value
        return this@EffectHandler.perform<Unit>(this)
    }
}

class RandomInt : Effect<Int> {
    val rng = Random(System.nanoTime())
    context(EffectHandler<*>)
    suspend operator fun invoke(): Int {
        return this@EffectHandler.perform<Int>(this)
    }
}

class Http : Effect<String> {
    var url: String? = null
    context(EffectHandler<*>)
    suspend operator fun invoke(url: String): String {
        this.url = url
        return this@EffectHandler.perform(this)
    }
}

class CanThrow<T> : Effect<T> {
    var exc: Throwable? = null
    context(EffectHandler<*>)
    suspend operator fun invoke(exc: Throwable): T {
        this.exc = exc
        return this@EffectHandler.perform<_>(this)
    }
}



