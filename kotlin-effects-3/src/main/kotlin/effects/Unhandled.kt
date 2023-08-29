package effects

class UnhandledEffectException : Throwable()

/**
 * Special marker used to alert a not handled effect.
 */
fun <R> unhandled(): R = throw UnhandledEffectException()