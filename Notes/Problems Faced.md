This note contains all the problems faced during the development of the prototype Library. The not will become handy when the thesis will be written.

**11/05/2023**

During the call, we spot five problems (not listed by a specific criteria):
1. Is the handler written correctly? For example, should I be able to resume always from an effect? What happen if I resume twice?
	* A possible solutions could be to use Kotlin Contracts
2. At the moment, there is no easy way for defining an handler capable of handling several effects (so, *mono-handlers*).
3. A lot of type verbosity. The current implementation, contained in the folder [Playground](../Playground/effects-1), contains a lot of type verbosity when there is to define an handler. A temporal solution would be to use `_` to automatically infer types.
4. Currently, the library hugely relies on the Coroutine mechanism provided by Kotlin. So, we have to take into account where the the function execution can be resume (we are talking about `CoroutineContext`). We thought two solutions: the first one, is to have a dedicated dispatcher for the effects, the second one, would be to handle the effects in the same thread they were performed.
5. Union Types: Kotlin does not have a way of defining Union Types (as mentioned in [[Designing a Library]]). This does not allow to define combined effects. For example: `effect StateAndAbort<X> = State<X> | Abort`.

