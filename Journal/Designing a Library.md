So, after the last meeting on 05/05/2023, we discussed about developing a tiny library for Algebraic Effect Handlers. I started looking for some Kotlin features, to implement effects similar to Unison and OCaml.

* For this implementation I would try to use *Context Receivers*.

From some analysis, it seems that Kotlin has some little limitations in its type system:
* There is no way of expressing a new [union-type](https://www.typescriptlang.org/docs/handbook/2/everyday-types.html#union-types) from existing types. For example, if I want to combine the `Yield<X>` and the `Log` effects, I cannot write `type Ex<X> = Yield<X> | Log`. 
* No variadic generic types.

I think those two problems can be "fixed" using a Kotlin Compiler Plugin, so it has to be further investigated later.

