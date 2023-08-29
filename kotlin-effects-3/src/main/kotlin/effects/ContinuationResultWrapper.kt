package effects

import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

class ContinuationResultWrapper<R>(
    private val coroutineName: String,
    private val effectfulScope: EffectfulScope<R>
) : Continuation<R?> {

    override val context: CoroutineContext
        get() = CoroutineName(coroutineName)


    var result: Result<R?>? = null

    override fun resumeWith(result: Result<R?>) {
        effectfulScope.status = EffectfulScope.EffectfulFunctionStatus.COMPUTED
        this.result = result
    }
}