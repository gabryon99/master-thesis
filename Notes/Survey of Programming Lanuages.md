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
* Do Be Do Be Do: https://arxiv.org/pdf/1611.09259.pdf -- Frank programming language implementing Effect Handlers
* Abstraction-Safe Effect Handlers via Tunneling: https://dl.acm.org/doi/pdf/10.1145/3290318
* https://github.com/lampepfl/dotty/pull/16626
* The error model: https://joeduffyblog.com/2016/02/07/the-error-model/
	
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
* [[Unison]]: this programming language implements effects using the concept of **abilities**
* The [Genus](https://www.cs.cornell.edu/projects/genus/) programming language

---

- With algebraic effect handlers, on the other hand, we can define effects as algebraic operations and then compose them using a simple syntax
- Algebraic effect handlers provide a way to handle effects that is more efficient than other approaches. They allow for effects to be handled dynamically at runtime, without the need for expensive stack manipulations.