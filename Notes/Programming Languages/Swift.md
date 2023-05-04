The Swift Programming Language #informal: Swift is a language developed mainly by Apple for their iOS and macOS devices (even tough it runs also on other OSes).

On the following [link](https://forums.swift.org/t/algebraic-effects/38769), a user was proposing to implement AEH into Swift, and how could the language could get some benefits from them. The user explains AEH as a sort of special annotation kind appearing in the function declaration. The user shows an example in Swift.

```swift
func getRandomVector(ofLength: Int) random -> [Double]{
	(0..<length).map{ _ in getrandom Double.self }
}
```

Defining the `random` effect in the function signature, the programmer cannot ignore the existence of the *random side effect*. And, they introduce a new keyword called `getrandom` for performing the effect (similar to `throw` with exceptions).

They proceed explaining how to call a function with an effect signature:
1. The function can be invoked by another function annotated with the same effect
2. Via special language construct that allow the developer to convert a random function into a non-random function; these language constructs may require you to provide additional context like, e.g., a random generator (a.k.a. **defining an handler**).

Digging further into case two, they propose the following approach:

```swift
func unsafeRandomCaller() -> Void {
	do {
		let randomVector = flip getRandomVector(ofLength: 42)
		doSomething(with: randomVector)
	}
	catch { continuation : (RandomGeneratorProtocol) -> Void in
		continuation(UsualRandomGenerator())
	}
}
```

Another proposal is the following one:

```swift
func unsafeRandomCaller2() -> Void {
	// Giving the context to the function
	runRandom(RandomGenerator()) {
		doSomething(flip getRandomVector(ofLength: 42))
	}
}
```

This may look a bit more natural, however it is a bit tricky to get the types right.

---

### Notes and Useful

* Sadly, the discussion on the forum blog ends on the same month of the post's creation (July 2020).
* Antipodean's quote: “*I have stumbled upon a pair of articles which discuss a generalized implementation of this idea, with the bold program of unifying notions such as exceptions, state resumption, iterators, domain specific effects and, yes, even asynchronous programming.*”