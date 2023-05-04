This document summarizes all the programming languages I found implementing Algebraic Effect Handlers, including proposal, and comments about them in forums.

## Links

### Papers, Forums, and Videos

* https://www.philipzucker.com/notes/Programming/continuations-effects/ #investigate
* Efficient compilation of algebraic effect handlers: https://dl.acm.org/doi/abs/10.1145/3485479 #investigate 
* Algebraic Effects: Swift #done https://forums.swift.org/t/algebraic-effects/38769
* Asynchrony with Algebraic Effects: https://www.youtube.com/watch?v=hrBq8R_kxI0 #video #koka #daan-leijen #investigate
* Retrofitting Effect Handlers onto OCaml: https://pldi21.sigplan.org/details/pldi-2021-papers/14/Retrofitting-Effect-Handlers-onto-OCaml #ocaml #investigate 
* Efficient compilation of algebraic effect handlers: https://dl.acm.org/doi/abs/10.1145/3485479 #ocaml #eff #cps #investigate 
* A survey of Algebraic Effect System by Ryan J. Kung #investigate https://ryankung.github.io/pdfs/2018-07-20-a-survey-of-algebraic-effect-system.pdf
* Algebraic Effect Handlers go Mainstream https://kcsrk.info/papers/effects_dagstuhl18.pdf #daan-leijen #investigate 
* Dart proposal: https://github.com/dart-lang/language/issues/2567 #dart 
* Algebraic Effect for the rest of us: https://overreacted.io/algebraic-effects-for-the-rest-of-us/
* Efficient Compilation of Algebraic Effect Handlers (Reddit / Video): https://www.reddit.com/r/ProgrammingLanguages/comments/vq86w1/efficient_compilation_of_algebraic_effect/
* Programming with Algebraic Effects and Handlers: https://math.andrej.com/wp-content/uploads/2012/03/eff.pdf
* Lenses, Transducers, and Algebraic Effects: https://ikrima.dev/dev-notes/prog-lang-theory/plt-lenses-transducers-effects/
* ==Zero-cost Effect Handlers by Staging (Technical Report)==: https://ps.informatik.uni-tuebingen.de/publications/schuster19zero.pdf
* Shallow Effect Handlers: https://homepages.inf.ed.ac.uk/slindley/papers/shallow-extended.pdf
* Suggest supporting algebraic effects #dart: https://www.reddit.com/r/dartlang/comments/xzu55m/suggest_supporting_algebraic_effects/

### Programming Languages

* Helium: https://github.com/Helium4Haskell/helium #investigate #daan-leijen #haskell #functional (last commit on 2019)
* Frank Programming Language: https://github.com/frank-lang/frank #investigate #haskell #effect-type-system #functional (last commit 2022)
* Effekt: https://effekt-lang.org/ #investigate #interesting #functional #effect-type-system 
* Eff: https://www.eff-lang.org/ #functional #investigate 
* Koka: https://koka-lang.github.io/koka/doc/book.html#sec-handlers #functional #daan-leijen #investigate 
	* Implementing Algebraic Effects in C: http://j.mp/2uaIcFE
		* Comments on Hacker News: https://news.ycombinator.com/item?id=14887341
* OCaml: 
	* Eio - Effects-Based Parallel IO for OCaml: https://github.com/ocaml-multicore/eio
* F#:
	* Why algebraic effects matter in F#: https://dev.to/shimmer/why-algebraic-effects-matter-in-f-3m7g
	* AlgEff - Algebraic Effect for F#: https://github.com/brianberns/AlgEff
	* Effect and Handlers: http://www.fssnip.net/jl/title/Effects-and-Handlers
* C#:
	* The trouble with Checked Exceptions: https://www.artima.com/articles/the-trouble-with-checked-exceptions
	* Effect Programming in C#: https://eiriktsarpalis.wordpress.com/2020/07/20/effect-programming-in-csharp/
* Scala:
	* Scala-Effekt: https://b-studios.de/scala-effekt/
	* ZIO effects: https://zio.dev/overview/creating-effects/
	* Safer exceptions for Scala: https://dl.acm.org/doi/10.1145/3486610.3486893
		* Discussion on Reddit: https://www.reddit.com/r/scala/comments/m50trg/martin_odersky_explains_the_motivations_behind/
		* GitHub Proposal: http://web.archive.org/web/20210314173714/https://github.com/dotty-staging/dotty/blob/add-safe-throws-2/docs/docs/reference/experimental/canthrow.md
* Rust: 
	* investigate the following crate https://docs.rs/async-std/latest/async_std/ and book https://rust-lang.github.io/async-book/
	* effing-mad: https://github.com/rosefromthedead/effing-mad
	* https://github.com/withoutboats/fehler
* Ante: http://antelang.org/

## Proposals

The Swift Programming Language #informal: Swift is a language developed mainly by Apple for their iOS and macOS devices (even tough it runs also on other OSes). On the following [link](https://forums.swift.org/t/algebraic-effects/38769), a user was proposing to implement AEH into Swift, and how could the language could get some benefits from them. The user explains AEH as a sort of special annotation kind appearing in the function declaration. The user shows an example in Swift.

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

### Notes and Useful

* Sadly, the discussion on the forum blog ends on the same month of the post's creation (July 2020).
* Antipodean's quote: “*I have stumbled upon a pair of articles which discuss a generalized implementation of this idea, with the bold program of unifying notions such as exceptions, state resumption, iterators, domain specific effects and, yes, even asynchronous programming.*”

---

- With algebraic effect handlers, on the other hand, we can define effects as algebraic operations and then compose them using a simple syntax
- Algebraic effect handlers provide a way to handle effects that is more efficient than other approaches. They allow for effects to be handled dynamically at runtime, without the need for expensive stack manipulations.