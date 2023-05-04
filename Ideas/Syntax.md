While reading about the Unison programming language, I thought a little bit about the syntax that Kotlin could use.

There are three sketches at the moment.

The **first** one:
```
fun filter<X, E>(tree: Tree<X>, f: (X) -> Bool 'E): 'E, 'Yield<X> Unit {  
	// ...  
}   
```

The **second** one:
```
fun filter<X, E>(tree: Tree<X>, f: (X) -> '[E] Bool): '[E, Yield<X>] Unit {  
	// ...  
}  
```

The **third** one:
```
fun filter<X, E>(tree: Tree<X>, f: (X) -> Bool/E): Unit/E,Yield<X> {  
	// ...  
} 

// With type inference
fun filter<X, E>(tree: Tree<X>, f: (X) -> Bool/_): Unit/_ {
	// ...
}
```

At the moment (03/05/2023), the preferred syntax is the **third one**. Even tough, I still have to finish reading the paper [[Abstraction-Safe Effect Handlers via Tunneling]].

---

Declaring effects with a syntax similar to enumerator?

```
effect Yield<T> {
	Yield(yielded: T)
}
```