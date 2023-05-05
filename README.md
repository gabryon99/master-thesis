## Extending Kotlin with Algebraic Effect Handlers

This folder contains all the notes for the Master Thesis @ ([University of Pisa](https://di.unipi.it/), [TUM](https://www.cit.tum.de/cit/)) + [JetBrains](https://www.jetbrains.com/).

### Description

The work consists of trying to extend the [Kotlin](https://kotlinlang.org/) programming language with **Algebraic Effect Handlers** (AEH).

AEH allow to model the control flow of a given program defining effects and how these should be handled. For instance, with algebraic effects
it is possible to define the Exception, Generator, Cooperative shecudling mechanisms.

Currently, AEH are not implemented inside Kotlin, we are investigating on how we should approach them, which is this thesis main's goal. If you are curious to
try them, some functional programming languages already implement AEH:

1. [Koka](https://koka-lang.github.io/). Koka is developed by Microsoft Research, and it one of the most famous for implementing AEH.
2. [OCaml](https://ocaml.org). OCaml has adopted AEH recently with its new version (5.0).
3. [Unison](https://www.unison-lang.org/). Unison implements AEH calling the **abilities**.

In addition, there exist some libraries to implement AEH, for Java/C/C++/Rust.
