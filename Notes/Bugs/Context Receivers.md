During the development of the library for Algebraic Effect Handlers, we faced a bug within the **Context Receivers** feature.

The test Kotlin Compiler version is `1.8.21`, with traget the JVM `1.8`.

So, after enableing the Context Receivers through the flag `-Xcontext-receivers` we wrote the following code.

```kotlin
abstract class AbstractContext {  
	abstract fun sayGoodBye(name: String): Unit  
	fun sayHello(name: String) = println("Hello $name!")  
}  
  
fun <AC: AbstractContext> foo(ctx: AC, f: context(AC) () -> Unit) {  
f(ctx)  
}  
  
fun main(args: Array<String>) {  
  
	val anonObject = object : AbstractContext() {  
		override fun sayGoodBye(name: String) = println("Goodbye $name!")  
	}  
	  
	foo<AbstractContext>(anonObject) {  
	  
		// The current lambda is within the AbstractContext.  
		  
		// I can invoke functions defined in the context  
		sayHello("Kotlin")  
		sayGoodBye("Kotlin")  

		// BUG!
		// But, I cannot reference the current context I am within  
		this.sayHello("Kotlin") // Raise: 'this' is not defined in this context  
	  
	}
}
```

When we try to reference `this` the compiler will complain us with the following warning: `'this' is not defined in this context`.