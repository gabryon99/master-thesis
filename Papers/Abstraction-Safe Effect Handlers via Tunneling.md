### Abstraction-Safe Effect Handlers via Tunneling

Authors: *YIZHOU ZHANG*, *ANDREW C. MYERS*
Date: 02 January 2019
DOI: 10.1145/3290318
File: [[Abstraction-Safe Effect Handlers via Tunneling.pdf]]

The paper shows how the effect handlers semantics are not *abstraction-safe*. In fact, the example shown explains how the abstraction of a given function could be broken due to the **effect handlers** scopes. Right now, the effect handler scopes is similar to the **dynamic scoping** technique, which can be confusing for the user.

As said by the abstract:
> *We demonstrate that abstraction-safe algebraic effect handlers are possible by giving them a new semantics*. ==The key insight is that code should only handle effects it is aware of.== 


The thecnique proposed by the authors to solve the issue consist of the use of *tuneling*, similar to the one used by *exceptions*. 

![[Pasted image 20230504132754.png|500]]

```
handle {
	// Some effectful operation
}
with {
	Effect1 -> (),
	Effect2 -> (),
}
```

The solution to the dynamic scoping of handler will be passing the static handler as an explicit parameter.

```
iterate[X](tr: Tree[X]): void/Yield[X] {
	// ...
}

// Into

iterate[X, h: Yield[X]](tr: Tree[X]): void/h {
	iterate[X, h](tr.left())
	h.yield(tr.value())
	iterate[X, h](tr.right())
}
```

The resulting method is polymorphic over a handler for `Yield[X]`, and the effectful computation in its body is handled by the handler.

The tuneling rewriting would be:

```
val fsize = ...
val g = fun[h: Yield[int]](x: int): bool / h { h.yield(x); f(x) }
try {

}
with H = new Yield[int]() {
	yield(x: int): void {
		// ...
	}
}
```